package com.example.fas.mapper.dto.deviceDto;

import com.example.fas.model.SensorData;
import com.example.fas.model.User;
import com.example.fas.model.enums.device.DeviceStatus;
import com.example.fas.model.enums.room.RoomType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DeviceResponseDto {
    private String deviceCode;

    private String nameDevice;

    private String description;

    private String room;

    private String ipV4Address;

    private Long ownerId;

    private Double safetyThreshold;

    private Double warningThreshold; // Ngưỡng cảnh báo

    private Double dangerThreshold; // Ngưỡng nguy cấp

    private String status;

    private String lastActiveTime;
}
