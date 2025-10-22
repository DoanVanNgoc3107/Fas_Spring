package com.example.fas.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.fas.dto.UserDto.UserRequestDto;
import com.example.fas.dto.UserDto.UserResponseDto;
import com.example.fas.model.User;

/**
 * Mapper helper for converting between {@link User} entity and its DTOs.
 *
 * <p>This class contains simple, stateless mapping methods. Keep mappings
 * lightweight and free of business logic — validation and complex transforms
 * should be performed in the service layer.</p>
 *
 * <p>Usage examples:
 * <ul>
 *   <li>Converting entity to response DTO: {@link #toDto(User)}</li>
 *   <li>Converting request DTO to entity: {@link #toEntity(UserRequestDto)}</li>
 * </ul>
 * </p>
 */
@Component
public class UserMapper {

    /**
     * Convert a {@link User} entity to {@link UserResponseDto}.
     *
     * <p>This method returns {@code null} when the input is {@code null}.
     * It only maps data fields and does not perform any business validation.</p>
     *
     * @param user the {@link User} entity to convert; may be {@code null}
     * @return a populated {@link UserResponseDto} or {@code null} if {@code user} is {@code null}
     */
    public UserResponseDto toDto(User user) {
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
                .phoneNumber(user.getPhoneNumber())
                .createdAt(String.valueOf(user.getCreatedAt()))
                .updatedAt(String.valueOf(user.getUpdatedAt()))
                .build();
    }

    /**
     * Convert a {@link UserRequestDto} to a {@link User} entity.
     *
     * <p>Note: this mapping creates a new {@link User} instance and fills
     * only basic fields provided by the request DTO. Business defaults
     * (e.g., role, status) or additional processing should be set in the service
     * layer after mapping.</p>
     *
     * @param dto the request DTO; may be {@code null}
     * @return a new {@link User} entity populated from the DTO, or {@code null} if {@code dto} is {@code null}
     */
    public User toEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .fullName(dto.getFirstName() + " " + dto.getLastName())
                .username(dto.getUsername())
                .identityCard(dto.getIdentityCard())
                .phoneNumber(dto.getPhoneNumber())
                .build();
    }

    /*
    * */
    public List<UserResponseDto> toDtoList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream().map(this::toDto).toList();
    }

}