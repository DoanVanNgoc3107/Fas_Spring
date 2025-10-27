package com.example.fas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Cho phép origins - Updated for production
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // Local development
                "https://your-production-domain.com" // Production domain
        ));

        // ✅ Cho phép HTTP methods - SỬA: dùng setAllowedMethods() thay vì
        // addAllowedMethod()
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // ✅ Cho phép headers - SỬA: dùng setAllowedHeaders() thay vì addAllowedHeader()
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // ✅ Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        // ✅ Apply CORS to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
