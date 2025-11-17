package com.example.fas.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class HomeController {
    @GetMapping
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            return "Hello, " + name + " (" + email + ")! You have successfully logged in via OAuth2.";
        }
        return "Hello, Guest! Please log in.";
    }
}