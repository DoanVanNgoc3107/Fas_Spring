package com.example.fas.security;

import com.example.fas.utils.CustomAuthenticationEntryPoint;
import com.example.fas.utils.CustomeAccessDeniedHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomsUserDetailsService customsUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomeAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(CustomsUserDetailsService customsUserDetailsService, PasswordEncoder passwordEncoder, JwtAuthenticationFilter jwtAuthenticationFilter, CustomeAccessDeniedHandler customAccessDeniedHandler, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customsUserDetailsService = customsUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // Bean daoAuthenticationProvider đã đúng
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customsUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource)).csrf(AbstractHttpConfigurer::disable) // Tắt CSRF

                .exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint).accessDeniedHandler(customAccessDeniedHandler))

                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**", "/oauth2/**", "/oauth2/authorization/**", "/login/oauth2/**", "/login").permitAll().requestMatchers("/api/users/**").permitAll().anyRequest().authenticated())

                // CHO PHÉP TẠO SESSION KHI CẦN (OAuth2 cần session để lưu AuthorizationRequest)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .formLogin(Customizer.withDefaults())

                // Cấu hình OAuth2 Login
                .oauth2Login(Customizer.withDefaults())

                // THÊM "Người Soát Vé" JWT vào đúng vị trí
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}