package com.example.fas.mapper.dto.UserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserRequestDto {
    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "First name must contain only letters and spaces")
    @Size(min = 2, message = "First name must be at least 2 characters long")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Last name must contain only letters and spaces")
    @Size(min = 2, message = "Last name must be at least 2 characters long")
    private String lastName;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password can not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Email(message = "Email should be valid")
    private String email;
}
