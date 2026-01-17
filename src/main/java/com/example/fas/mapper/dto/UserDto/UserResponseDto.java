package com.example.fas.mapper.dto.UserDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String avatarUrl;
    private String provider;
    private String status;
    private String role;
    private List<String> deviceCodes;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;
}