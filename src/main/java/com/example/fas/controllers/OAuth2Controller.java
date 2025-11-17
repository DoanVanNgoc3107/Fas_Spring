package com.example.fas.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * OAuth2 API Controller
 * Redirects API requests to actual OAuth2 endpoints
 */
@RestController
@RequestMapping("/api/v1/oauth2")
public class OAuth2Controller {

    /**
     * Redirect /api/v1/oauth2/authorization/{provider} 
     * to /oauth2/authorization/{provider}
     * 
     * @param provider OAuth2 provider (google, github, facebook)
     * @param response HttpServletResponse for redirect
     */
    @GetMapping("/authorization/{provider}")
    public void initiateOAuth2Login(
            @PathVariable String provider,
            HttpServletResponse response) throws IOException {
        
        // Redirect to actual OAuth2 endpoint (without /api/v1 prefix)
        response.sendRedirect("/oauth2/authorization/" + provider);
    }
}
