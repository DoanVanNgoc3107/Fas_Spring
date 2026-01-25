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

        // Use setAllowedOriginPatterns to support wildcards
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*", // Local development (any port)
                "https://doanvanngoc3107.store",
                "https://www.doanvanngoc3107.store", // With www subdomain
                "https://*.doanvanngoc3107.store", // All subdomains
                "https://*.vercel.app", // All Vercel deployments
                "https://fas-*.vercel.app" // Specific pattern for FAS frontend
        ));

        // Cho phép ...
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));

        // Cho phép tất cả headers
        configuration.setAllowedHeaders(List.of("*"));

        // (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        // Apply CORS to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
