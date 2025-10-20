package com.example.fas.services;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.enums.Role;
import com.example.fas.enums.Status;
import com.example.fas.mapper.UserMapper;
import com.example.fas.model.User;
import com.example.fas.repositories.UserRepository;
import com.example.fas.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Khởi tạo data chung, nếu cần (ví dụ: mock passwordEncoder.encode trả về "encodedPassword")
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void testCreateUserSuccess() {
        // Arrange: Chuẩn bị input và mock behavior
        UserRequestDto dto = UserRequestDto.builder()
            .firstName("John")
            .lastName("Doe")
            .username("johndoe")
            .password("Password123")
            .phoneNumber("0345515986")
            .identityCard("031205019283")
            .build();
        // Tạo User stub với đầy đủ trường
        Instant now = Instant.now();
        User unsaved = User.builder()
            .fullName("John Doe")
            .username("johndoe")
            .password("encodedPassword")
            .status(Status.ACTIVE)
            .role(Role.USER)
            .identityCard("031205019283")
            .phoneNumber("0345515986")
            .createdAt(now)
            .updatedAt(now)
            .build();
        // Tạo User trả về sau save với id
        User saved = User.builder()
            .id(1L)
            .fullName(unsaved.getFullName())
            .username(unsaved.getUsername())
            .password(unsaved.getPassword())
            .status(unsaved.getStatus())
            .role(unsaved.getRole())
            .identityCard(unsaved.getIdentityCard())
            .phoneNumber(unsaved.getPhoneNumber())
            .createdAt(unsaved.getCreatedAt())
            .updatedAt(unsaved.getUpdatedAt())
            .build();
        when(userMapper.toEntity(any(UserRequestDto.class))).thenReturn(unsaved);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(saved);
        when(userMapper.toDto(any(User.class))).thenReturn(UserResponseDto.builder()
             .id(1L)
             .fullName("John Doe")
             .username("johndoe")
             .phoneNumber("0345515986")
             .identityCard("031205019283")
             .status("ACTIVE")
             .role("USER")
             .createdAt("2023-10-10")
             .updatedAt("2023-10-10")
             .build());

        // Act: Gọi method
        UserResponseDto result = userService.createUser(dto);

        // Assert: Kiểm tra kết quả
        assertNotNull(result);
        verify(userRepository).saveAndFlush(any(User.class));
    }
}
