package com.example.fas.controllers;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.authDto.LoginResponseDto;
import com.example.fas.security.JwtService;
import com.example.fas.serviceImpl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DaoAuthenticationProvider daoAuthenticationProvider;

    private final UserServiceImpl userService;

    private final JwtService jwtService;

    public AuthController(DaoAuthenticationProvider daoAuthenticationProvider, JwtService jwtService, UserServiceImpl userService) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRequestDto>> registerPage(@Valid @RequestBody UserRequestDto userDto) {
        userService.createUser(userDto);
        var apiResponse = ApiResponse.success("Registration successful.", userDto);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginPage(@Valid @RequestBody LoginRequestDto loginDto) {
        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword()
        );
        Authentication auth = this.daoAuthenticationProvider.authenticate(user);
        String token = this.jwtService.generateToken(auth.getName());
        var loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(token);
        loginResponseDto.setUserDto(userService.getUserByUsername(loginDto.getUsername()));
        var apiResponse = ApiResponse.success("Login success.", loginResponseDto);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutPage(HttpServletRequest request) {
        var apiResponse = ApiResponse.success("Logout successful.", null);
        return ResponseEntity.ok(apiResponse);
    }
}