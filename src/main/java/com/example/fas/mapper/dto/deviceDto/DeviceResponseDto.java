package com.example.fas.mapper.dto.deviceDto;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class DeviceResponseDto {

    private Long id;

    private String deviceCode;

    private String nameDevice;

    private String description;

    private String room;

    private String ipV4Address;

    private Long ownerId;

    private Double safetyThreshold;

    private Double warningThreshold; // Ngưỡng cảnh báo

    private Double dangerThreshold; // Ngưỡng nguy cấp

    private String status; // ACTIVE, WARNING, DANGER

    private String lastActiveTime;
}
