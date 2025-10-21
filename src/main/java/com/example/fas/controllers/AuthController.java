package com.example.fas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fas.dto.authDto.LoginRequestDto;
import com.example.fas.model.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DaoAuthenticationProvider daoAuthenticationProvider;

    public AuthController(DaoAuthenticationProvider daoAuthenticationProvider) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginRequestDto>> loginPage(@Valid @RequestBody LoginRequestDto loginDto) {
        
    }
}
