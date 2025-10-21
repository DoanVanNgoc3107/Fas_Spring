package com.example.fas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Vô hiệu hóa CSRF để dễ dàng kiểm thử API (chỉ nên dùng trong môi trường phát triển)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/auth/**", "/api/users/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}