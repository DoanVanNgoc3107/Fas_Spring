package com.example.fas.mapper.dto.SensorDto;

import com.example.fas.model.enums.TypeSensor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SensorDataRequestDto {
    @NotBlank(message = "Device code must not be blank")
    private String deviceCode;

    @NotNull(message = "Value must not be null")
    @Min(value = -1, message = "Value must be greater than or equal to -1")
    private Double value;

    @NotNull(message = "TypeSensor must not be null")
    private TypeSensor typeSensor;
}
