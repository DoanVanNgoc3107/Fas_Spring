package com.example.fas.dto.UserDto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    private Long id;
    private String fullName;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$"
            , message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    private String phoneNumber;
    @Pattern(regexp = "^\\d{12}$", message = "CCCD must be exactly 12 digits")
    private String identityCard;
    private String status;
}
