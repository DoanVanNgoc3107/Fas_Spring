package com.example.fas.dto.UserDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String username;
    private String phoneNumber;
    private String status;
    private String role;
    private String identityCard;
    private String createdAt;
    private String updatedAt;
}