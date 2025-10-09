package com.example.fas.services;

import com.example.fas.model.User;

public interface UserService {
    // POST /users
    User createUser(User user);

    // PUT /users/{id}
    User updateUser(Long id, User user);

    // User updateUserByPhoneNumber(String phoneNumber, UserUpdateRequest userUpdateRequest);

    User deletedUser(Long id);

    User restoreUser(Long id);

    User hasAuthenticated(Long id);

    // GET /users/{id}
    User getUserById(Long id);
    
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
    void validateUser(User user);

    void validateUpdateUser(User user);
}


