package com.example.fas.mapper.dto.authDto;

import com.example.fas.mapper.dto.UserDto.UserResponseDto;
import lombok.Data;

@Data
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresIn;
    private long refreshTokenExpiresIn;
    private UserResponseDto userDto;
}
