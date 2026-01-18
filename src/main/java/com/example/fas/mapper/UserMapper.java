package com.example.fas.mapper;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.fas.model.Device;
import org.springframework.stereotype.Component;

import com.example.fas.mapper.dto.UserDto.UserRequestDto;
import com.example.fas.mapper.dto.UserDto.UserResponseDto;
import com.example.fas.model.User;

/**
 * Mapper helper for converting between {@link User} entity and its DTOs.
 *
 * <p>
 * This class contains simple, stateless mapping methods. Keep mappings
 * lightweight and free of business logic — validation and complex transforms
 * should be performed in the service layer.
 * </p>
 *
 * <p>
 * Usage examples:
 * <ul>
 * <li>Converting entity to response DTO: {@link #toDto(User)}</li>
 * <li>Converting request DTO to entity: {@link #toEntity(UserRequestDto)}</li>
 * </ul>
 * </p>
 */
@Component
public class UserMapper implements EntityMapper<UserResponseDto, User, UserRequestDto> {

    /**
     * Convert a {@link User} entity to {@link UserResponseDto}.
     *
     * <p>
     * This method returns {@code null} when the input is {@code null}.
     * It only maps data fields and does not perform any business validation.
     * </p>
     *
     * @param user the {@link User} entity to convert; may be {@code null}
     * @return a populated {@link UserResponseDto} or {@code null} if {@code user}
     * is {@code null}
     */
    public UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .status(user.getUserStatus() != null ? user.getUserStatus().name() : null)
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .provider(user.getProvider() != null ? user.getProvider().name() : "NONE")
                .createdAt(Instant.parse(String.valueOf(user.getCreatedAt())))
                .updatedAt(Instant.parse(String.valueOf(user.getUpdatedAt())))
                .deviceId(user.getDevices() != null ? user.getDevices().stream().map(Device::getId).collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Convert a {@link UserRequestDto} to a {@link User} entity.
     *
     * <p>
     * Note: this mapping creates a new {@link User} instance and fills
     * only basic fields provided by the request DTO. Business defaults
     * (e.g., role, userStatus) or additional processing should be set in the service
     * layer after mapping.
     * </p>
     *
     * @param requestDto the request DTO; may be {@code null}
     * @return a new {@link User} entity populated from the DTO, or {@code null} if
     * {@code dto} is {@code null}
     */
    public User toEntity(UserRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        return User.builder()
                .fullName(requestDto.getFirstName() + " " + requestDto.getLastName())
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .build();
    }

    /**
     * Convert a list of {@link User} entities to a list of {@link UserResponseDto}.
     *
     * <p>This method returns {@code null} when the input list is {@code null}.
     * It only maps data fields and does not perform any business validation.</p>
     *
     * @param users the list of {@link User} entities to convert; may be {@code
     *              null}
     * @return a list of populated {@link UserResponseDto} or {@code null} if {@code
     * users} are {@code null}
     */
    public List<UserResponseDto> toDtoList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream().map(this::toDto).toList();
    }

    /**
     * Convert a list of {@link User} entities to a set of {@link UserResponseDto}.
     *
     * @param users the list of {@link User} entities to convert; may be {@code null}
     * @return a set of populated {@link UserResponseDto} or {@code null} if {@code users} are {@code null}
     *
     */
    public Set<UserResponseDto> toDtoSet(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

}