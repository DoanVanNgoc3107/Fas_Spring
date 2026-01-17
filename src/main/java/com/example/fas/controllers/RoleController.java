package com.example.fas.controllers;

import com.example.fas.mapper.dto.RoleDto.RoleRequestDto;
import com.example.fas.mapper.dto.RoleDto.RoleResponseDto;
import com.example.fas.mapper.dto.RoleDto.RoleUpdateDto;
import com.example.fas.model.ApiResponse;
import com.example.fas.repositories.services.serviceImpl.RoleServiceImp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleServiceImp roleServiceImpl;

    public RoleController(RoleServiceImp roleServiceImpl) {
        this.roleServiceImpl = roleServiceImpl;
    }

    /**
     * Get all roles
     * @return ResponseEntity<ApiResponse<Set<RoleResponseDto>>>
     * */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Set<RoleResponseDto>>> getAllRoles() {
        var res = new ApiResponse<>(
                HttpStatus.OK,
                "Roles retrieved successfully",
                roleServiceImpl.getRoles(),
                null
        );
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * Create a new role
     * @param roleRequestDto RoleRequestDto
     * @return ResponseEntity<ApiResponse<RoleResponseDto>>
     * */
    @PostMapping("/")
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(@Valid @RequestBody RoleRequestDto roleRequestDto) {
        var res = new ApiResponse<RoleResponseDto>(
                HttpStatus.CREATED,
                "Role created successfully",
                roleServiceImpl.createRole(roleRequestDto),
                null
        );
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    /**
     * Update an existing role
     * @param roleUpdateDto RoleUpdateDto
     * @param id Long
     * @return ResponseEntity<ApiResponse<RoleResponseDto>>
     * */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(@Valid @RequestBody RoleUpdateDto roleUpdateDto, @PathVariable("id") Long id) {
        var res = new ApiResponse<>(
                HttpStatus.UPGRADE_REQUIRED,
                "Role updated successfully",
                roleServiceImpl.updateRole(id, roleUpdateDto),
                null
        );
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
