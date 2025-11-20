package com.example.fas.mapper.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdateDto {
    @NotNull(message = "Role ID to update cannot be null")
    private Long roleIdUpdate;

    private String roleNameUpdate;

    private String descriptionUpdate;
}
