package com.example.fas.mapper.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RoleResponseDto {
    @NotNull(message = "Role ID cannot be null")
    private Long roleId;

    @NotBlank(message = "Role name cannot be blank")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name must be uppercase letters and underscores only")
    private String roleName;
}
