package com.example.fas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomsUserDetailsService customsUserDetailsService;

    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomsUserDetailsService customsUserDetailsService, PasswordEncoder passwordEncoder) {
        this.customsUserDetailsService = customsUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /* Đây là phương thức cấu hình DaoAuthenticationProvider để xác thực người dùng sử dụng
     * CustomsUserDetailsService và PasswordEncoder đã được định nghĩa trước đó.
     * DaoAuthenticationProvider sẽ sử dụng CustomsUserDetailsService để tải thông tin người dùng từ
     * cơ sở dữ liệu và PasswordEncoder để so sánh mật khẩu đã mã hóa.
     * */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customsUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /*
     * This is the security filter chain configuration method that defines the security rules for incoming requests.
     * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF để dễ dàng kiểm thử API
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Sử dụng HTTP Basic Authentication
        return http.build();
    }
}