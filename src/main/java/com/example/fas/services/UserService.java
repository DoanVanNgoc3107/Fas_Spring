package com.example.fas.services;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.dto.UserDto.UserUpdateRequest;
import com.example.fas.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Pageable;

public interface UserService {
    // POST /users
    UserResponseDto createUser(UserRequestDto user);

    // PUT /users/{id}
    UserResponseDto updateUser(UserUpdateRequest userUpdateRequest);

    void restoreUser(Long id);

    UserResponseDto isAdmin(Long id);

    UserResponseDto isUser(Long id);

    void banUser(Long id);

    // GET /users/{id}
    UserResponseDto getUserById(Long id);

    List<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserByUsername(String username);

    UserResponseDto getUserByIdentityCard(String identityCard);

    UserResponseDto getUserByPhoneNumber(String phoneNumber);

    UserResponseDto  getUserByFullName(String fullName);

    UserResponseDto getUserByEmail(String email);

    UserResponseDto getUserByAvatarUrl(String avatarUrl);

    UserResponseDto getUserByProviderId(String providerId);

    Set<UserResponseDto> getUsersByStatus(String status);

    Set<UserResponseDto> getUsersByRole(String role);

    Set<UserResponseDto> getUsersBySocialProvider(String provider);

    UserResponseDto getInfoUserCurrent(Authentication authentication);

    // DELETE /users/{id}
    void deleteUserById(Long id);

    void deleteUserByUsername(String username);
    
    void deleteUserByIdentityCard(String identityCard);

    // Balance
    BigDecimal getBalanceById(Long id);

    void updateBalanceById(Long id, BigDecimal newBalance);

    void increaseBalance(Long id, BigDecimal amount);

    void decreaseBalance(Long id, BigDecimal amount);

    // Hàm tiện ích để validate dữ liệu người dùng
    void validateUser(UserRequestDto user);

    void validateUserId(Long id);

    void validateAmount(BigDecimal amount);

    void validateUserByIdentityCard(String identityCard);

    User getUserEntityById(Long id);
}


