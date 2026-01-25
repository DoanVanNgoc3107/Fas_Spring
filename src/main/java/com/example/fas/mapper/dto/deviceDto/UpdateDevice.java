package com.example.fas.mapper.dto.deviceDto;

import lombok.Data;

@Data
public class UpdateDevice {
    private Long id;
    private String deviceName;
    private String ipv4Address;
    private String description;
    private Double safetyThreshold;  // Ngưỡng an toàn
    private Double warningThreshold; // Ngưỡng cảnh báo
    private Double dangerThreshold;  // Ngưỡng nguy cấp
}
