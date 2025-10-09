package com.example.fas.dto.UserDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    private Long id;
    private String fullName;
    private String password;
    private String phoneNumber;
    private String identityCard;
    private String citizenId;
}
