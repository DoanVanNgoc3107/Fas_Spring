package com.example.fas.controllers;

import com.example.fas.mapper.dto.UserDto.UserRequestDto;
import com.example.fas.mapper.dto.UserDto.UserResponseDto;
import com.example.fas.mapper.dto.authDto.LoginRequestDto;
import com.example.fas.mapper.dto.authDto.LoginResponseDto;
import com.example.fas.mapper.dto.authDto.RefreshTokenRequestDto;
import com.example.fas.repositories.services.serviceImpl.exceptions.auth.AccessTokenInvalidException;
import com.example.fas.repositories.services.serviceImpl.exceptions.auth.LoginFailedException;
import com.example.fas.repositories.services.serviceImpl.exceptions.auth.RefreshTokenInvalidException;
import com.example.fas.config.security.JwtService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fas.model.ApiResponse;
import com.example.fas.repositories.services.UserService;

import io.jsonwebtoken.JwtException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(JwtService jwtService, UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Handles user login requests by validating the provided credentials and generating JWT tokens.
     *
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequestDto.getUsername(),
                    loginRequestDto.getPassword()
            );

            // Authenticate using AuthenticationManager (which uses CustomsUserDetailsService + PasswordEncoder)
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String username = authentication.getName();
            String accessToken = jwtService.generateAccessToken(username);
            String refreshToken = jwtService.generateRefreshToken(username);
            LoginResponseDto responseDto = buildLoginResponse(username, accessToken, refreshToken);
            var response = new ApiResponse<>(
                    HttpStatus.OK,
                    "Login successful",
                    responseDto,
                    null);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException | LoginFailedException ex) {
            var errorResponse = new ApiResponse<LoginResponseDto>(
                    HttpStatus.UNAUTHORIZED,
                    "Login failed",
                    null,
                    ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (AuthenticationException ex) {
            var errorResponse = new ApiResponse<LoginResponseDto>(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed",
                    null,
                    Map.of("reason", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Handles user registration by creating a new user with the provided information.
     * <p>
     * public ResponseEntity<ApiResponse<UserRequestDto>> register (@Valid @RequestBody UserRequestDto userDto) {taining an ApiResponse with a success message and the registered user data
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRequestDto>> registerPage(@Valid @RequestBody UserRequestDto userDto) {
        userService.createUser(userDto);
        var apiResponse = ApiResponse.success("Registration successful.", userDto);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Hàm này xử lý login của người dùng bằng cách xác thực thông tin đăng nhập và tạo JWT token.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getInfoCurrentUser(Authentication authentication) {
        try {
            UserResponseDto currentUser = userService.getInfoUserCurrent(authentication);
            var response = new ApiResponse<>(
                    HttpStatus.OK,
                    "Current user retrieved successfully",
                    currentUser,
                    null);
            return ResponseEntity.ok(response);
        } catch (AccessTokenInvalidException ex) {
            var errorResponse = new ApiResponse<UserResponseDto>(
                    HttpStatus.UNAUTHORIZED,
                    "Failed to retrieve current user",
                    null,
                    Map.of("reason", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Handles token refresh requests by validating the provided refresh token
     * and issuing new JWT tokens if valid.
     *
     * @param request The refresh token request data.
     * @return A ResponseEntity containing an ApiResponse with new JWT tokens or an error message
     */
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

        } catch (JwtException | RefreshTokenInvalidException ex) {
            ApiResponse<LoginResponseDto> errorResponse = new ApiResponse<>(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token",
                    null,
                    Map.of("reason", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Handles user logout requests
     *
     * @return A ResponseEntity containing an ApiResponse with a logout success message.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutPage() {
        var apiResponse = ApiResponse.success("Logout successful.", null);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Builds the login response DTO containing tokens and user information.
     *
     * @param username     The username of the authenticated user.
     * @param accessToken  The generated access token.
     * @param refreshToken The generated refresh token.
     * @return A LoginResponseDto containing the tokens and user information.
     */
    private LoginResponseDto buildLoginResponse(String username, String accessToken, String refreshToken) {
        LoginResponseDto dto = new LoginResponseDto();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken);
        dto.setAccessTokenExpiresIn(jwtService.getAccessTokenTtl());
        dto.setRefreshTokenExpiresIn(jwtService.getRefreshTokenTtl());
        dto.setUser(userService.getUserByUsername(username));
        return dto;
    }
}

