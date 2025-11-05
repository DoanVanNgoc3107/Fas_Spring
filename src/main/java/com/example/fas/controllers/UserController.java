package com.example.fas.controllers;

import com.example.fas.serviceImpl.UserServiceImpl;
import org.springframework.web.bind.annotation.*;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.model.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static com.example.fas.model.ApiResponse.success;

@RestController
@RequestMapping("/api/v1/users")
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
        var response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully",
                userServiceImpl.getUserById(id),
                null
        );
        return ResponseEntity.ok(response);
    }

    /*
     * Tạo users bằng tài khoản admin
     * */
    @PostMapping("/")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody UserRequestDto userRequest) {
        UserResponseDto createdUser = userServiceImpl.createUser(userRequest);
        ApiResponse<UserResponseDto> response = new ApiResponse<>(HttpStatus.CREATED, "User created successfully", createdUser, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> listUserDto = userServiceImpl.getAllUsers();
        var response = new ApiResponse<List<UserResponseDto>>(HttpStatus.OK, "All users retrieved successfully", listUserDto, null);
        return ResponseEntity.ok(response);
    }

    /*
     * */
    @PutMapping("/is-admin/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> isAdmin(@PathVariable Long id) {
        var response = new ApiResponse<UserResponseDto>(HttpStatus.OK, "Set role ADMIN success.!", userServiceImpl.isAdmin(id), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /*
     * */
    @PutMapping("/is-user/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> isUser(@PathVariable Long id) {
        var response = new ApiResponse<>(HttpStatus.OK, "Set role USER success.!", userServiceImpl.isUser(id), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* Hàm khóa tài khoản user
     * API */
    @PutMapping("/banned/{id}")
    public ResponseEntity<ApiResponse<Void>> banned(@PathVariable Long id) {
        userServiceImpl.banUser(id);
        return new ResponseEntity<>(success("Banned user success.!", null), HttpStatus.NO_CONTENT);
    }

    /* Hàm khôi phục tài khoản user
     * API
     * */
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long id) {
        userServiceImpl.restoreUser(id);
        return new ResponseEntity<>(success("Restore user success.!", null), HttpStatus.NO_CONTENT);
    }

    // Make identity and fullname endpoints explicit to avoid ambiguous path variables
    @GetMapping("/identity/{identityCard}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByIdentityCard(@PathVariable String identityCard) {
        var response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully",
                userServiceImpl.getUserByIdentityCard(identityCard),
                null
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fullname/{fullName}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByFullName(@PathVariable String fullName) {
        var response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully",
                userServiceImpl.getUserByFullName(fullName),
                null
        );
        return ResponseEntity.ok(response);
    }

    // Disambiguate delete endpoints (delete by id vs delete by username)
    @PutMapping("/delete/id/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserById(@PathVariable Long id) {
        userServiceImpl.deleteUserById(id);
        return new ResponseEntity<>(success("Delete user success.!", null), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/delete/username/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUserByUsername(@PathVariable String username) {
        userServiceImpl.deleteUserByUsername(username);
        return new ResponseEntity<>(success("Delete user success.!", null), HttpStatus.NO_CONTENT);
    }

    // LOGIC BALANCE
    @GetMapping("/balance/{id}")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalanceById(@PathVariable Long id) {
        BigDecimal balance = userServiceImpl.getBalanceById(id);
        var response = new ApiResponse<>(HttpStatus.OK, "Balance retrieved successfully", balance, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/balance/update/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBalanceById(@PathVariable Long id, @RequestBody BigDecimal newBalance) {
        userServiceImpl.updateBalanceById(id, newBalance);
        return new ResponseEntity<>(success("Update balance success.!", null), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/balance/increase/{id}")
    public ResponseEntity<ApiResponse<Void>> increaseBalance(@PathVariable Long id, @RequestBody BigDecimal amount) {
        userServiceImpl.increaseBalance(id, amount);
        return new ResponseEntity<>(success("Increase balance success.!", null), HttpStatus.NO_CONTENT);
    }
}
