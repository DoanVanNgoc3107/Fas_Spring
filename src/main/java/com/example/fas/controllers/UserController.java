package com.example.fas.controllers;

import com.example.fas.config.security.JwtService;
import com.example.fas.repositories.services.serviceImpl.UserServiceImpl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.example.fas.mapper.dto.UserDto.UserRequestDto;
import com.example.fas.mapper.dto.UserDto.UserResponseDto;
import com.example.fas.model.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.fas.model.ApiResponse.success;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;
    private final JwtService jwtService;

    public UserController(UserServiceImpl userServiceImpl, JwtService jwtService) {
        this.userServiceImpl = userServiceImpl;
        this.jwtService = jwtService;
    }

    /**
     * Get information of user by ID
     *
     * @param id ID của user cần lấy thông tin
     * @return ResponseEntity
     *
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        var response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully",
                userServiceImpl.getUserById(id),
                null);
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo mới user
     *
     * @param userRequest - Thông tin user cần tạo
     * @return ResponseEntity - Trả về user đã được tạo
     *
     */
    @PostMapping("/")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody UserRequestDto userRequest) {
        UserResponseDto createdUser = userServiceImpl.createUser(userRequest);
        ApiResponse<UserResponseDto> response = new ApiResponse<>(HttpStatus.CREATED, "User created successfully",
                createdUser, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lấy tất cả user đã phân trang
     *
     * @param pageable - Phân trang
     * @return ResponseEntity Trả về danh sách user đã phân trang
     *
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(
            @RequestParam("currents") Optional<String> currentOptional,
            @RequestParam("sizes") Optional<String> sizeOptional,
            Pageable pageable) {
        int current_page = Integer.parseInt(currentOptional.orElse(""));
        int size_page = Integer.parseInt(sizeOptional.orElse(""));

        Pageable page = PageRequest.of(current_page - 1, size_page, Sort.by(Sort.Direction.ASC, "id"));

        List<UserResponseDto> userPage = userServiceImpl.getAllUsers(page);
        var response = new ApiResponse<>(
                HttpStatus.OK,
                "All users retrieved successfully",
                userPage,
                null);
        return ResponseEntity.ok(response);
    }


    /**
     * This function updates user information based on the provided user update
     * request.
     *
     * @param id The user update request containing updated user information.
     * @return A ResponseEntity containing an ApiResponse with the updated user
     * information.
     */
    @PutMapping("/is-admin/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> isAdmin(@PathVariable Long id) {
        var response = new ApiResponse<>(HttpStatus.OK, "Set role ADMIN success.!", userServiceImpl.isAdmin(id), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * This function updates user information based on the provided user update
     * request.
     *
     * @param id The ID of the user to be updated.
     * @return A ResponseEntity containing an ApiResponse with the updated user
     * information.
     */
    @PutMapping("/is-user/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> isUser(@PathVariable Long id) {
        var response = new ApiResponse<>(HttpStatus.OK, "Set role USER success.!", userServiceImpl.isUser(id), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Hàm cập nhật trạng thái banned cho user
     * API
     *
     * @param id ID của user cần cập nhật
     * @return ResponseEntity trả về user đã được cập nhật
     *
     */
    @PutMapping("/banned/{id}/{days}")
    public ResponseEntity<ApiResponse<Void>> banned(@PathVariable Long id, @PathVariable Integer days) {
        userServiceImpl.bannedUser(id, days);
        return new ResponseEntity<>(success("Banned user success.!", null), HttpStatus.NO_CONTENT);
    }

    /**
     * Endpoint restore user having been soft-deleted
     *
     * @param id ID of the user to be restored
     * @return ResponseEntity
     *
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long id) {
        userServiceImpl.restoreUser(id);
        return new ResponseEntity<>(success("Restore user success.!", null), HttpStatus.NO_CONTENT);
    }

    /**
     * @param identityCard The identity card number of the user to retrieve.
     * @return A ResponseEntity containing an ApiResponse with the user information.
     * @brief This function retrieves a user by their identity card number.
     */
    @GetMapping("/identity/{identityCard}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByIdentityCard(@PathVariable String identityCard) {
        var response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully",
                userServiceImpl.getUserByIdentityCard(identityCard),
                null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/full-name/{fullName}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByFullName(@PathVariable String fullName) {
        var response = new ApiResponse<>(
                HttpStatus.OK,
                "User retrieved successfully",
                userServiceImpl.getUserByFullName(fullName),
                null);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user information from JWT access token
     * Frontend chỉ cần gửi token trong header: Authorization: Bearer {token}
     *
     * @param authHeader The Authorization header containing Bearer token
     * @return ResponseEntity containing current user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUserFromToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from the "Bearer xxx" format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header", null, null));
            }

            String token = authHeader.substring(7);

            // Extract username from token
            String username = jwtService.extractUsername(token);

            // Get fresh user data from database
            UserResponseDto currentUser = userServiceImpl.getUserByUsername(username);

            var apiResponse = ApiResponse.success("Current user fetched successfully.", currentUser);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, "Invalid or expired token", null, e.getMessage()));
        }
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
    public ResponseEntity<ApiResponse<Void>> updateBalanceById(@PathVariable Long id,
                                                               @RequestBody BigDecimal newBalance) {
        userServiceImpl.updateBalanceById(id, newBalance);
        return new ResponseEntity<>(success("Update balance success.!", null), HttpStatus.NO_CONTENT);
    }

    @PutMapping("/balance/increase/{id}")
    public ResponseEntity<ApiResponse<Void>> increaseBalance(@PathVariable Long id, @RequestBody BigDecimal amount) {
        userServiceImpl.increaseBalance(id, amount);
        return new ResponseEntity<>(success("Increase balance success.!", null), HttpStatus.NO_CONTENT);
    }
}
