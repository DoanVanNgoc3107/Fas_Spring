package com.example.fas.mapper.dto.UserDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePassword {
    @NotNull(message = "User ID cannot be null")
    private Long id;
    @NotBlank(message = "Old password cannot be blank")
    private String oldPassword;
    @NotBlank(message = "New password cannot be blank")
    private String newPassword;
}
