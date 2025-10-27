package com.example.fas.dto.authDto;

import com.example.fas.dto.UserDto.UserResponseDto;
import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private UserResponseDto userDto;
}
