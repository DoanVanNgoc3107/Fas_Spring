package com.example.fas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🔐 CẤU HÌNH AUTHENTICATION PROVIDER
//                .authenticationProvider(authenticationProvider())

                // 🌐 CẤU HÌNH PHÂN QUYỀN
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/users/**").permitAll()     // API đăng nhập/đăng ký - public
                        .requestMatchers("/api/public/**").permitAll()      // API public - ai cũng truy cập được
                        .anyRequest().authenticated()// Các API khác yêu cầu đăng nhập
                )

//                // 🔒 CẤU HÌNH SESSION
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Không lưu session
//                )

                // 🛡️ TẮT CSRF (cho API đơn giản)
                .csrf(csrf -> csrf.disable())

//                // 🔐 CẤU HÌNH JWT RESOURCE SERVER
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
//                )
        ;

        return http.build();
    }
}