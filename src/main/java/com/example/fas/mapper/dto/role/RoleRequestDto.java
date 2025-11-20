package com.example.fas.mapper.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequestDto {
    @NotBlank(message = "Role name cannot be blank")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name must be uppercase letters and underscores only")
    private String roleName;

    @NotBlank(message = "Role description cannot be blank")
    private String description;
}
