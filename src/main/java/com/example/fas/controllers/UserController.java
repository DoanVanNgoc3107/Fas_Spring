package com.example.fas.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.model.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.fas.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully", 
                user,
                null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody UserRequestDto userRequest) {
        UserResponseDto createdUser = userService.createUser(userRequest);
        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                HttpStatus.CREATED,
                "User created successfully",
                createdUser,
                null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
