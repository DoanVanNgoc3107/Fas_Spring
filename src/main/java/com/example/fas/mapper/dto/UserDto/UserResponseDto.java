package com.example.fas.mapper.dto.UserDto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String username;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private String provider;
    private Integer coins;
    private String providerId;
    private BigDecimal balance;
    private String status;
    private String role;
    private String identityCard;
    private String createdAt;
    private String updatedAt;
}