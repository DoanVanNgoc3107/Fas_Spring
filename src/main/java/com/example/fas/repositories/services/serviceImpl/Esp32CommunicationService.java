package com.example.fas.repositories.services.serviceImpl;

import com.example.fas.mapper.dto.deviceDto.ThresholdResponse;
import com.example.fas.mapper.dto.deviceDto.ThresholdUpdateRequest;
import com.example.fas.model.Device;
import com.example.fas.repositories.services.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service để giao tiếp với ESP32 qua HTTP
 * Gửi các lệnh cập nhật cấu hình xuống thiết bị ESP32
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Esp32CommunicationService {

    private final DeviceRepository deviceRepository;
    private final RestTemplate restTemplate;

    // Token xác thực ESP32 (cấu hình trong application.properties)
    @Value("${esp32.api.token:esp32_secret_token_2026}")
    private String esp32ApiToken;

    // Timeout cho HTTP request (milliseconds)
    @Value("${esp32.api.timeout:5000}")
    private int requestTimeout;

    /**
     * Gửi request cập nhật ngưỡng xuống ESP32
     *
     * @param deviceId ID của thiết bị trong database
     * @param request  Thông tin ngưỡng mới
     * @return ThresholdResponse từ ESP32
     */
    public ThresholdResponse updateThreshold(Long deviceId, ThresholdUpdateRequest request) {
        // 1. Tìm thiết bị để lấy IP
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với ID: " + deviceId));

        // 2. Kiểm tra thiết bị có online không
        if (device.getIpV4Address() == null || device.getIpV4Address().isEmpty()) {
            throw new RuntimeException("Thiết bị chưa có địa chỉ IP");
        }

        // 3. Validate ngưỡng: safety < warning < danger
        validateThresholds(request);

        // 4. Gửi request xuống ESP32
        String esp32Url = "http://" + device.getIpV4Address() + "/api/threshold";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(esp32ApiToken);

            HttpEntity<ThresholdUpdateRequest> entity = new HttpEntity<>(request, headers);

            log.info("Sending threshold update to ESP32 at {}: {}", esp32Url, request);

            ResponseEntity<ThresholdResponse> response = restTemplate.exchange(
                    esp32Url,
                    HttpMethod.PUT,
                    entity,
                    ThresholdResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ThresholdResponse esp32Response = response.getBody();

                // 5. Cập nhật ngưỡng trong database nếu ESP32 xác nhận thành công
                if (Boolean.TRUE.equals(esp32Response.getSuccess())) {
                    device.setSafetyThreshold(request.getSafety().doubleValue());
                    device.setWarningThreshold(request.getWarning().doubleValue());
                    device.setDangerThreshold(request.getDanger().doubleValue());
                    deviceRepository.save(device);

                    log.info("Threshold updated successfully for device {}: safety={}, warning={}, danger={}",
                            device.getDeviceCode(), request.getSafety(), request.getWarning(), request.getDanger());
                }

                return esp32Response;
            } else {
                throw new RuntimeException("ESP32 trả về response không hợp lệ");
            }

        } catch (Exception e) {
            log.error("Failed to update threshold for device {}: {}", device.getDeviceCode(), e.getMessage());
            throw new RuntimeException("Không thể kết nối với ESP32: " + e.getMessage());
        }
    }

    /**
     * Lấy ngưỡng hiện tại từ ESP32
     *
     * @param deviceId ID của thiết bị
     * @return ThresholdResponse từ ESP32
     */
    public ThresholdResponse getThresholdFromDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với ID: " + deviceId));

        if (device.getIpV4Address() == null || device.getIpV4Address().isEmpty()) {
            throw new RuntimeException("Thiết bị chưa có địa chỉ IP");
        }

        String esp32Url = "http://" + device.getIpV4Address() + "/api/threshold";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(esp32ApiToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ThresholdResponse> response = restTemplate.exchange(
                    esp32Url,
                    HttpMethod.GET,
                    entity,
                    ThresholdResponse.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get threshold from device {}: {}", device.getDeviceCode(), e.getMessage());
            throw new RuntimeException("Không thể kết nối với ESP32: " + e.getMessage());
        }
    }

    /** 
     * Kiểm tra sức khỏe của ESP32
     * @param deviceId ID của thiết bị
     * @return true nếu ESP32 đang hoạt động
     */
    public boolean healthCheck(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với ID: " + deviceId));

        if (device.getIpV4Address() == null || device.getIpV4Address().isEmpty()) {
            return false;
        }

        String esp32Url = "http://" + device.getIpV4Address() + "/api/health";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(esp32Url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Health check failed for device {}: {}", device.getDeviceCode(), e.getMessage());
            return false;
        }
    }

    /**
     * Validate các ngưỡng: safety < warning < danger
     */
    private void validateThresholds(ThresholdUpdateRequest request) {
        if (request.getSafety() >= request.getWarning()) {
            throw new IllegalArgumentException("Ngưỡng an toàn phải nhỏ hơn ngưỡng cảnh báo");
        }
        if (request.getWarning() >= request.getDanger()) {
            throw new IllegalArgumentException("Ngưỡng cảnh báo phải nhỏ hơn ngưỡng nguy hiểm");
        }
    }

    /**
     * Kích hoạt cảnh báo khẩn cấp - đưa ESP32 về trạng thái NGUY HIỂM
     * ESP32 sẽ kích hoạt còi, đèn cảnh báo và gửi thông báo
     * 
     * @param deviceId ID của thiết bị
     * @return Kết quả từ ESP32
     */
    public String triggerEmergencyAlert(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với ID: " + deviceId));

        if (device.getIpV4Address() == null || device.getIpV4Address().isEmpty()) {
            throw new RuntimeException("Thiết bị chưa có địa chỉ IP");
        }

        String esp32Url = "http://" + device.getIpV4Address() + ":8080/api/alert/trigger";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(esp32ApiToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Triggering emergency alert for device {} at {}", device.getDeviceCode(), esp32Url);

            ResponseEntity<String> response = restTemplate.exchange(
                    esp32Url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Emergency alert triggered successfully for device {}", device.getDeviceCode());
                return "Cảnh báo khẩn cấp đã được kích hoạt";
            } else {
                throw new RuntimeException("ESP32 trả về mã lỗi: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to trigger alert for device {}: {}", device.getDeviceCode(), e.getMessage());
            throw new RuntimeException("Không thể kích hoạt cảnh báo: " + e.getMessage());
        }
    }

    /**
     * Reset cảnh báo - đưa hệ thống về trạng thái ban đầu
     * 
     * @param deviceId ID của thiết bị
     * @return Kết quả từ ESP32
     */
    public String resetAlert(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với ID: " + deviceId));

        if (device.getIpV4Address() == null || device.getIpV4Address().isEmpty()) {
            throw new RuntimeException("Thiết bị chưa có địa chỉ IP");
        }

        String esp32Url = "http://" + device.getIpV4Address() + "/api/alert/reset";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(esp32ApiToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Resetting alert for device {} at {}", device.getDeviceCode(), esp32Url);

            ResponseEntity<String> response = restTemplate.exchange(
                    esp32Url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Alert reset successfully for device {}", device.getDeviceCode());
                return "Cảnh báo đã được reset";
            } else {
                throw new RuntimeException("ESP32 trả về mã lỗi: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to reset alert for device {}: {}", device.getDeviceCode(), e.getMessage());
            throw new RuntimeException("Không thể reset cảnh báo: " + e.getMessage());
        }
    }
}
