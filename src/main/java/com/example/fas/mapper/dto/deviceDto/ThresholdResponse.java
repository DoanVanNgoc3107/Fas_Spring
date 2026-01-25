package com.example.fas.mapper.dto.deviceDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response từ ESP32 sau khi cập nhật threshold
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdResponse {
    private Boolean success;
    private ThresholdValues thresholds;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThresholdValues {
        private Integer safety;
        private Integer warning;
        private Integer danger;
    }
}
