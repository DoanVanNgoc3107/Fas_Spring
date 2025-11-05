package com.example.fas.controllers;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.authDto.LoginResponseDto;
import com.example.fas.dto.authDto.RefreshTokenRequestDto;
import com.example.fas.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fas.dto.authDto.LoginRequestDto;
import com.example.fas.model.ApiResponse;
import com.example.fas.services.UserService;

import io.jsonwebtoken.JwtException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
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
                loginDto.getUsername(), loginDto.getPassword());
        var authentication = authenticationManager.authenticate(user);
        String username = authentication.getName();
        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);
        LoginResponseDto loginResponseDto = buildLoginResponse(username, accessToken, refreshToken);
        var apiResponse = ApiResponse.success("Login success", loginResponseDto);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        String refreshToken = request.getRefreshToken();
        if (!StringUtils.hasText(refreshToken)) {
            ApiResponse<LoginResponseDto> errorResponse = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST,
                    "Refresh token is required",
                    null,
                    Map.of("reason", "refresh token blank"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            String username = jwtService.extractUsername(refreshToken);
            if (!jwtService.isRefreshTokenValid(refreshToken, username)) {
                ApiResponse<LoginResponseDto> errorResponse = new ApiResponse<>(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token",
                        null,
                        Map.of("reason", "expired or mismatched"));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String newAccessToken = jwtService.generateAccessToken(username);
            String newRefreshToken = jwtService.generateRefreshToken(username);
            LoginResponseDto responseDto = buildLoginResponse(username, newAccessToken, newRefreshToken);
            ApiResponse<LoginResponseDto> apiResponse = ApiResponse.success("Token refreshed successfully", responseDto);
            return ResponseEntity.ok(apiResponse);
        } catch (JwtException | IllegalArgumentException ex) {
            ApiResponse<LoginResponseDto> errorResponse = new ApiResponse<>(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token",
                    null,
                    Map.of("reason", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutPage(HttpServletRequest request) {
        var apiResponse = ApiResponse.success("Logout successful.", null);
        return ResponseEntity.ok(apiResponse);
    }

    private LoginResponseDto buildLoginResponse(String username, String accessToken, String refreshToken) {
        LoginResponseDto dto = new LoginResponseDto();
        dto.setToken(accessToken);
        dto.setRefreshToken(refreshToken);
        dto.setAccessTokenExpiresIn(jwtService.getAccessTokenTtl());
        dto.setRefreshTokenExpiresIn(jwtService.getRefreshTokenTtl());
        dto.setUserDto(userService.getUserByUsername(username));
        return dto;
    }
}