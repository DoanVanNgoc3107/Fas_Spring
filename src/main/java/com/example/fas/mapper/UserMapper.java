package com.example.fas.mapper;

import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.model.User;

public class UserMapper {
    public static UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .identityCard(user.getIdentityCard())
                .citizenId(user.getCitizenId())
                .build();
    }

    public static User toEntity(UserResponseDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User().builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .username(dto.getUsername())
                .status(dto.getStatus() != null ? User.Status.valueOf(dto.getStatus()) : null)
                .role(dto.getRole() != null ? User.Role.valueOf(dto.getRole()) : null)
                .identityCard(dto.getIdentityCard())
                .citizenId(dto.getCitizenId())
                .build();
                return user;
    }
}