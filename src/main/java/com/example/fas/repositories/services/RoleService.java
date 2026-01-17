package com.example.fas.repositories.services;

import com.example.fas.mapper.dto.RoleDto.RoleRequestDto;
import com.example.fas.mapper.dto.RoleDto.RoleResponseDto;
import com.example.fas.mapper.dto.RoleDto.RoleUpdateDto;

import java.util.Set;

public interface RoleService {
    RoleResponseDto createRole(RoleRequestDto roleRequestDto);

    RoleResponseDto updateRole(long id, RoleUpdateDto roleUpdate);

    boolean deleteRoleById(Long id);

    RoleResponseDto getRoleById(Long id);

    Set<RoleResponseDto> getRoles();

    void validateIdRole(Long id);

    void validateRole(RoleRequestDto roleRequestDto);

    void validateRoleUpdate(RoleUpdateDto roleUpdateDto);

    RoleResponseDto getRoleByName(String roleName);
}
