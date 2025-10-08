package com.example.fas.dto.UserDto;

import lombok.Data;

@Data
public class UserRequestDto {
    private String fullName;
    private String username;
    private String password;
    private String phoneNumber;
}
