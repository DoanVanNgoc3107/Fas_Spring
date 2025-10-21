package com.example.fas.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Username not blank")
    private String username;

    @NotBlank(message = "Password not blank")
    private String password;
}