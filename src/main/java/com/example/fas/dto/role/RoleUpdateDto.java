package com.example.fas.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateDto {
    @NotNull(message = "Role ID to update cannot be null")
    private Long roleIdUpdate;

    private String roleNameUpdate;

    private String descriptionUpdate;
}
