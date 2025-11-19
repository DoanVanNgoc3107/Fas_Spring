package com.example.fas.services;

import com.example.fas.dto.role.RoleRequestDto;
import com.example.fas.dto.role.RoleResponseDto;
import com.example.fas.dto.role.RoleUpdateDto;

import java.util.Optional;
import java.util.Set;

public interface RoleService {
    RoleResponseDto createRole(RoleRequestDto roleRequestDto);

    RoleResponseDto updateRole(RoleUpdateDto roleUpdate);

    boolean deleteRoleById(Long id);

    RoleResponseDto getRoleById(Long id);

    Set<RoleResponseDto> getRoles();

    void validateIdRole(Long id);

    void validateRole(RoleRequestDto roleRequestDto);

    void validateRoleUpdate(RoleUpdateDto roleUpdateDto);

    RoleResponseDto getRoleByName(String roleName);
}
