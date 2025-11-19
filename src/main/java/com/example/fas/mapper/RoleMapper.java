package com.example.fas.mapper;

import com.example.fas.dto.role.RoleRequestDto;
import com.example.fas.dto.role.RoleResponseDto;
import com.example.fas.dto.role.RoleUpdateDto;
import com.example.fas.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    /**
     * Convert a {@link Role} entity to a {@link RoleResponseDto}.
     * *
     * <p>
     * This method returns {@code null} when the input is {@code null}.
     * It only maps data fields and does not perform any business validation.
     * </p>
     * * @param role the {@link Role} entity to convert; may be {@code null}
     */
    public RoleResponseDto toDto(Role role) {
        if (role == null) {
            return null;
        }
        RoleResponseDto dto = new RoleResponseDto();
        dto.setRoleId(role.getId());
        dto.setRoleName(role.getRoleName());
        return dto;
    }

    /**
     * Convert a {@link RoleRequestDto} to a {@link Role} entity.
     * *
     * <p>
     * This method returns {@code null} when the input is {@code null}.
     * It only maps data fields and does not perform any business validation.
     * </p>
     * * @param requestDto the {@link RoleRequestDto} to convert; may be {@code null}
     */
    public Role toEntity(RoleRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        Role role = new Role();
        role.setRoleName(requestDto.getRoleName());
        role.setDescription(requestDto.getDescription());
        return role;
    }

    /**
     * Convert a list of {@link Role} entities to a set of {@link RoleResponseDto}.
     *
     * <p>
     * This method returns an empty set when the input list is {@code null} or empty.
     * It only maps data fields and does not perform any business validation.
     * </p>
     *
     * @param roleList the list of {@link Role} entities to convert; may be {@code null}
     * @return a set of {@link RoleResponseDto}; never {@code null}
     */
    public Set<RoleResponseDto> toSet(List<Role> roleList) {
        return roleList.stream().map(this::toDto).collect(Collectors.toSet());
    }
}
