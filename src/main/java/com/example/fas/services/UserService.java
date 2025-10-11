package com.example.fas.services;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.dto.UserDto.UserUpdateRequest;
import com.example.fas.model.User;

public interface UserService {
    // POST /users
    UserResponseDto createUser(UserRequestDto user);

    // PUT /users/{id}
    User updateUser(Long id, UserUpdateRequest userUpdateRequest);

    // User updateUserByPhoneNumber(String phoneNumber, UserUpdateRequest userUpdateRequest);

    User deletedUser(Long id);

    User restoreUser(Long id);

    User hasAuthenticated(Long id);

    // GET /users/{id}
    UserResponseDto getUserById(Long id);
    
    User getUserByUsername(String username);

    User getUserByIdentityCard(String identityCard);

    User getUserByCitizenId(String citizenId);

    User getUserByPhoneNumber(String phoneNumber);

    User getUserByFullName(String fullName);

    // DELETE /users/{id}
    void deleteUserById(Long id);

    void deleteUserByUsername(String username);
    
    void deleteUserByIdentityCard(String identityCard);

    void deleteUserByCitizenId(String citizenId);

    // Validate user details
    void validateUser(UserRequestDto user);

    void validateCitizenId(String citizenId);

    void validateUpdateUser(UserUpdateRequest user);
}


