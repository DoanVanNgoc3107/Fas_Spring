package com.example.fas.mapper.dto.deviceDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterDevice {
    @NotBlank(message = "Device code must not be blank")
    private String deviceCode;

    @NotBlank(message = "Device name must not be blank")
    private String deviceName;

    private String description;

    @NotNull(message = "User ID must not be null")
    private Long userId;

    @NotBlank(message = "IP address must not be blank")
    private String ipV4Address;
}