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
import org.springframework.security.config.http.SessionCreationPolicy; // Thêm import này
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Thêm import này
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomsUserDetailsService customsUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Đã inject

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomeAccessDeniedHandler customAccessDeniedHandler;

    // Constructor đã đúng
    public SecurityConfig(CustomsUserDetailsService customsUserDetailsService, PasswordEncoder passwordEncoder,
                          JwtAuthenticationFilter jwtAuthenticationFilter, CustomeAccessDeniedHandler customAccessDeniedHandler,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))

                // Bắt đầu định nghĩa quy tắc truy cập
                .authorizeHttpRequests(auth -> auth
                        // QUY TẮC 1: Cho phép tất cả gọi API login và đăng ký
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // CHUYỂN SANG STATELESS (Không tạo session)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Cấu hình OAuth2 Login
                .oauth2Login(Customizer.withDefaults())

                // THÊM "Người Soát Vé" JWT vào đúng vị trí
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}