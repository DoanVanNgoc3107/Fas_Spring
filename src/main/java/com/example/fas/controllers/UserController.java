package com.example.fas.controllers;

import com.example.fas.serviceImpl.UserServiceImpl;
import org.springframework.web.bind.annotation.*;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.model.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    /*
     * Lấy info user bằng ID của user.
     * @param Long id
     * @return ResponseEntity
     * */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        UserResponseDto userDto = userServiceImpl.getUserById(id);
        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully",
                userDto,
                null);
        return new ResponseEntity<>(ApiResponse.success("Had created user complete.!", userDto), HttpStatus.CREATED);
    }

    /*
     * Tạo users bằng tài khoản admin
     * */
    @PostMapping("/")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody UserRequestDto userRequest) {
        UserResponseDto createdUser = userServiceImpl.createUser(userRequest);
        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                HttpStatus.CREATED,
                "User created successfully",
                createdUser,
                null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> listUserDto = userServiceImpl.getAllUsers();
        var response = new ApiResponse<List<UserResponseDto>>(
                HttpStatus.OK,
                "All users retrieved successfully",
                listUserDto,
                null);
        return ResponseEntity.ok(response);
    }

    /*
     * */
    @PutMapping("/is-admin/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> isAdmin(@PathVariable Long id) {
        var response = new ApiResponse<UserResponseDto>(
                HttpStatus.OK,
                "Set role ADMIN success.!",
                userServiceImpl.isAdmin(id),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
