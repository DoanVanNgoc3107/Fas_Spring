package com.example.fas.mapper.dto.deviceDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để cập nhật ngưỡng cảnh báo cho thiết bị ESP32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdUpdateRequest {
    
    @NotNull(message = "Ngưỡng an toàn không được để trống")
    @Min(value = 0, message = "Ngưỡng an toàn phải lớn hơn 0")
    private Integer safety;
    
    @NotNull(message = "Ngưỡng cảnh báo không được để trống")
    @Min(value = 0, message = "Ngưỡng cảnh báo phải lớn hơn 0")
    private Integer warning;
    
    @NotNull(message = "Ngưỡng nguy hiểm không được để trống")
    @Min(value = 0, message = "Ngưỡng nguy hiểm phải lớn hơn 0")
    private Integer danger;
}
