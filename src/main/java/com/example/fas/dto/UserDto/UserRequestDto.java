package com.example.fas.dto.UserDto;

import lombok.Data;

@Data
public class UserRequestDto {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String identityCard;
    private String phoneNumber;
}
