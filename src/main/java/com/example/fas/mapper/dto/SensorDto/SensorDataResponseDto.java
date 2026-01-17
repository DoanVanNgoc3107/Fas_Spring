package com.example.fas.mapper.dto.SensorDto;

import com.example.fas.model.enums.TypeSensor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataResponseDto {
    private Long id;
    private String deviceCode;
    private String deviceName;
    private Double value;
    private TypeSensor typeSensor;
    private Instant timestamp;
}
