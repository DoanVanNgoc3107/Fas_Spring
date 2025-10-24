package com.example.fas.dto.socialDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocialLogin {
    private String username;
    private String password;
}
