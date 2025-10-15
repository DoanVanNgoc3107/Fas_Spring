package com.example.fas.services;

import java.util.List;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.dto.UserDto.UserUpdateRequest;
import com.example.fas.model.User;

public interface UserService {
    // POST /users
    UserResponseDto createUser(UserRequestDto user);

    // PUT /users/{id}
    User updateUser(Long id, UserUpdateRequest userUpdateRequest);

    void restoreUser(Long id);

    // GET /users/{id}
    UserResponseDto getUserById(Long id);

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserByUsername(String username);

    UserResponseDto getUserByIdentityCard(String identityCard);

    UserResponseDto getUserByCitizenId(String citizenId);

    UserResponseDto getUserByPhoneNumber(String phoneNumber);

    UserResponseDto  getUserByFullName(String fullName);

    // DELETE /users/{id}
    void deleteUserById(Long id);

    void deleteUserByUsername(String username);
    
    void deleteUserByIdentityCard(String identityCard);

    void deleteUserByCitizenId(String citizenId);

    // Validate user details
    void validateUser(UserRequestDto user);

    void validateCitizenId(String citizenId);

    void validateUserId(Long id);

    void validateUpdateUser(UserUpdateRequest user);
}


