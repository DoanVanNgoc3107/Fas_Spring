package com.example.fas.dto.UserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    @NotNull(message = "User ID cannot be null")
    private Long id;

    @Size(min = 2, message = "First name must be at least 2 characters long")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @Size(min = 2, message = "Last name must be at least 2 characters long")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Last name must contain only letters and spaces")
    private String lastName;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$"
            , message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @Pattern(regexp = "^(\\+84|0)(3[2-9]|5[689]|7[0-9]|8[1-5]|9[0-46-9])[0-9]{7}$", message = "Invalid phone number")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    private String email;

    private String avatarUrl;

    @Pattern(regexp = "^\\d{12}$", message = "CCCD must be exactly 12 digits")
    private String identityCard;
}
