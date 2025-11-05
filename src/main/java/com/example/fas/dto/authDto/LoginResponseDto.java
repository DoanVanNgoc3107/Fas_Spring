package com.example.fas.dto.authDto;

import com.example.fas.dto.UserDto.UserResponseDto;
import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private String refreshToken;
    private long accessTokenExpiresIn;
    private long refreshTokenExpiresIn;
    private UserResponseDto userDto;
}
