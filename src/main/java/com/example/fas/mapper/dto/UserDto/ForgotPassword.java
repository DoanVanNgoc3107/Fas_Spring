package com.example.fas.mapper.dto.UserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPassword {
    @NotBlank(message = "Username not blank")
    private String username;

    @Email(message = "Email should be valid")
    private String email;
}
