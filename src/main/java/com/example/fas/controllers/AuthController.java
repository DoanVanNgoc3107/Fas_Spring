package com.example.fas.controllers;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.dto.authDto.LoginResponseDto;
import com.example.fas.dto.authDto.RefreshTokenRequestDto;
import com.example.fas.exceptions.auth.AccessTokenInvalidException;
import com.example.fas.exceptions.auth.RefreshTokenInvalidException;
import com.example.fas.security.JwtService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * Handles user registration.
     *
     * @param userDto The user registration data.
     * @return A ResponseEntity containing an ApiResponse with the registration result.
     *
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRequestDto>> registerPage(@Valid @RequestBody UserRequestDto userDto) {
        userService.createUser(userDto);
        var apiResponse = ApiResponse.success("Registration successful.", userDto);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * This function handles user login by authenticating the provided credentials,
     * generating JWT tokens, and returning them in the response.
     *
     * @param loginDto The login request data containing username and password.
     * @return A ResponseEntity containing an ApiResponse with a login success message and tokens.
     *
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginPage(@Valid @RequestBody LoginRequestDto loginDto) {
        // Hàm xác thực người dùng với username và password được cung cấp
        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        var authentication = authenticationManager.authenticate(user);
        String username = authentication.getName();
        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);
        LoginResponseDto loginResponseDto = buildLoginResponse(username, accessToken, refreshToken);

        var apiResponse = ApiResponse.success("Login success", loginResponseDto);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Get current authenticated user information from JWT token.
     * Frontend chỉ cần gửi token trong Authorization header.
     * 
     * Best Practice: Endpoint này nên ở AuthController vì liên quan đến authentication.
     * 
     * @param authHeader Authorization header chứa Bearer token
     * @return ResponseEntity chứa thông tin user hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getInfoCurrentUser(Authentication authentication) throws AccessTokenInvalidException{
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
     *
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
     * Handles user logout requests.
     *
     * @return A ResponseEntity containing an ApiResponse with a logout success message.
     *
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
     *
     */
    private LoginResponseDto buildLoginResponse(String username, String accessToken, String refreshToken) {
        LoginResponseDto dto = new LoginResponseDto();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken);
        dto.setAccessTokenExpiresIn(jwtService.getAccessTokenTtl());
        dto.setRefreshTokenExpiresIn(jwtService.getRefreshTokenTtl());
        dto.setUserDto(userService.getUserByUsername(username));
        return dto;
    }
}