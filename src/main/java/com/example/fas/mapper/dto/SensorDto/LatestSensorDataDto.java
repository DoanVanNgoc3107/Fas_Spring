package com.example.fas.mapper.dto.SensorDto;

import com.example.fas.model.enums.TypeSensor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO để trả về dữ liệu cảm biến mới nhất
 * Dùng cho dashboard/real-time monitoring
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestSensorDataDto {
    private String deviceCode;
    private String deviceName;
    private Double mq2Value; // Giá trị MQ2 (khói/gas) mới nhất
    private Instant mq2Timestamp;
    private String deviceStatus; // ACTIVE, WARNING, DANGER
    private Instant lastActiveTime;
}
