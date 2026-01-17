package com.example.fas.mapper.dto.deviceDto;

import lombok.Data;

@Data
public class CheckConnectDeviceRequest {
    private String deviceCode;
    private String ipAddress;
    private String accessToken;
}
