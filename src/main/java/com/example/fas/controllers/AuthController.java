package com.example.fas.controllers;

import javax.naming.AuthenticationException;

import com.example.fas.dto.authDto.LoginResponseDto;
import com.example.fas.exceptions.auth.LoginFailedException;
import com.example.fas.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
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

    private final JwtService jwtService;

    public AuthController(DaoAuthenticationProvider daoAuthenticationProvider, JwtService jwtService) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginPage(@Valid @RequestBody LoginRequestDto loginDto) {
        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword());
        Authentication auth = this.daoAuthenticationProvider.authenticate(user);
        String token = this.jwtService.generateToken(auth.getName());
        var loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(token);
        var apiResponse = ApiResponse.success("Login success.", loginResponseDto);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutPage() {        // Invalidate the JWT token or perform any logout logic here
        // Invalidate the JWT token or perform any logout logic here
        var apiResponse = ApiResponse.success("Logout successful.", null);
        return ResponseEntity.ok(apiResponse);
    }
}