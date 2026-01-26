package com.example.fas.controllers;

import com.example.fas.config.websocket.ESP32WebSocketHandler;
import com.example.fas.mapper.dto.SensorDto.LatestSensorDataDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataRequestDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataResponseDto;
import com.example.fas.mapper.dto.deviceDto.UpdateDevice;
import com.example.fas.mapper.dto.deviceDto.DeviceResponseDto;
import com.example.fas.mapper.dto.deviceDto.RegisterDevice;
import com.example.fas.mapper.dto.deviceDto.ThresholdResponse;
import com.example.fas.mapper.dto.deviceDto.ThresholdUpdateRequest;
import com.example.fas.model.ApiResponse;
import com.example.fas.model.enums.TypeSensor;
import com.example.fas.repositories.services.serviceImpl.DeviceServiceImpl;
import com.example.fas.repositories.services.serviceImpl.Esp32CommunicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceServiceImpl deviceService;
    private final Esp32CommunicationService esp32Service;
    private final ESP32WebSocketHandler webSocketHandler;

    /**
     * Lấy thông tin của thiết bị theo ID
     * 
     * @param id ID thiết bị
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> getDeviceByCode(@PathVariable Long id) {
        try {
            var response = new ApiResponse<>(
                    HttpStatus.OK,
                    "Lấy thông tin thiết bị thành công.!",
                    deviceService.getDeviceById(id),
                    null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lấy thiết bị thất bại: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy thông tin của thiết bị dựa vào mã thiết bị
     * 
     * @param deviceCode Mã thiết bị
     * @return Thông tin thiết bị
     */
    @GetMapping("/code/{deviceCode}")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> getDeviceByCode(@PathVariable String deviceCode) {
        try {
            var response = new ApiResponse<>(
                    HttpStatus.OK,
                    "Lấy thông tin thiết bị thành công.!",
                    deviceService.getDeviceByCode(deviceCode),
                    null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lấy thiết bị thất bại: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách tất cả thiết bị
     * 
     * @return Danh sách thiết bị
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<DeviceResponseDto>>> getAllDevices() {
        try {
            var response = new ApiResponse<>(
                    HttpStatus.OK,
                    "Lấy danh sách thiết bị thành công.!",
                    deviceService.getAllDevices(),
                    null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lấy danh sách thiết bị thất bại: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách thiết bị của user
     * 
     * @param userId ID của user
     * @return Danh sách thiết bị của user
     */
    @GetMapping("/list/user/{userId}")
    public ResponseEntity<ApiResponse<List<DeviceResponseDto>>> getDevicesByUserId(@PathVariable Long userId) {
        try {
            var response = new ApiResponse<>(
                    HttpStatus.OK,
                    "Lấy danh sách thiết bị của user thành công.!",
                    deviceService.getDevicesByUserId(userId),
                    null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lấy danh sách thiết bị thất bại: " + e.getMessage(), null));
        }
    }

    /**
     * Đăng ký thiết bị mới cho user
     *
     * @param request request Thông tin đăng ký thiết bị
     */
    @PostMapping("/register/device")
    public ResponseEntity<ApiResponse<String>> registerDevice(@Valid @RequestBody RegisterDevice request) {
        deviceService.registerDevice(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thiết bị thành công", null));
    }

    /**
     * Nhận dữ liệu cảm biến từ thiết bị
     *
     * @param request Dữ liệu cảm biến từ thiết bị
     */
    @PostMapping("/data")
    public ResponseEntity<ApiResponse<String>> receiveData(@Valid @RequestBody SensorDataRequestDto request) {
        deviceService.processSensorData(request);
        return ResponseEntity.ok(ApiResponse.success("Dữ liệu đã được xử lý thành công", null));
    }

    /**
     * Lấy tất cả dữ liệu cảm biến của một thiết bị (có phân trang)
     */
    @GetMapping("/{deviceCode}/sensor-data")
    public ResponseEntity<ApiResponse<Page<SensorDataResponseDto>>> getSensorDataByDevice(
            @PathVariable String deviceCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<SensorDataResponseDto> data = deviceService.getSensorDataByDeviceCode(deviceCode, pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    /**
     * Lấy dữ liệu cảm biến theo loại (MQ2 hoặc DHT22)
     */
    @GetMapping("/{deviceCode}/sensor-data/{typeSensor}")
    public ResponseEntity<ApiResponse<Page<SensorDataResponseDto>>> getSensorDataByType(
            @PathVariable String deviceCode,
            @PathVariable TypeSensor typeSensor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<SensorDataResponseDto> data = deviceService.getSensorDataByDeviceCodeAndType(
                deviceCode, typeSensor, pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    /**
     * Lấy dữ liệu mới nhất của thiết bị (cho dashboard real-time)
     */
    @GetMapping("/{id}/latest")
    public ResponseEntity<ApiResponse<LatestSensorDataDto>> getLatestSensorData(
            @PathVariable Long id) {

        LatestSensorDataDto data = deviceService.getLatestSensorData(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    /**
     * Lấy dữ liệu trong khoảng thời gian
     */
    @GetMapping("/{id}/sensor-data/range")
    public ResponseEntity<ApiResponse<List<SensorDataResponseDto>>> getSensorDataByTimeRange(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        List<SensorDataResponseDto> data = deviceService.getSensorDataByTimeRange(
                id, startTime, endTime);

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    /**
     * Lấy dữ liệu cảm biến gần đây nhất (ví dụ: 10 bản ghi gần nhất)
     */
    @GetMapping("/{id}/recent")
    public ResponseEntity<ApiResponse<List<SensorDataResponseDto>>> getRecentSensorData(
            @PathVariable Long id) {

        List<SensorDataResponseDto> data = deviceService.getRecentSensorData(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    @GetMapping("/{id}/sensor-data/last")
    public ResponseEntity<ApiResponse<List<SensorDataResponseDto>>> getSensorDataLastHours(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int hours) {

        // Chỉ chấp nhận 1, 7, 24 theo yêu cầu
        if (hours != 1 && hours != 7 && hours != 24) {
            return ResponseEntity.ok(ApiResponse.error("Tham số 'hours' phải là 1, 7 hoặc 24", null));
        }

        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(hours, ChronoUnit.HOURS);

        List<SensorDataResponseDto> data = deviceService.getSensorDataByTimeRange(id, startTime, endTime);

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    /**
     * Lấy dữ liệu cảm biến mới sau một timestamp cụ thể (cho incremental update)
     */
    @GetMapping("/{id}/sensor-data/after")
    public ResponseEntity<ApiResponse<List<SensorDataResponseDto>>> getSensorDataAfterTimestamp(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant afterTimestamp) {

        Instant endTime = Instant.now();
        List<SensorDataResponseDto> data = deviceService.getSensorDataByTimeRange(id, afterTimestamp, endTime);

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    // ==================== ESP32 THRESHOLD MANAGEMENT ====================

    /**
     * Cập nhật ngưỡng cảnh báo cho thiết bị ESP32
     * Server gửi request xuống ESP32 để cập nhật threshold
     * 
     * @param id      ID của thiết bị
     * @param request Thông tin ngưỡng mới (safety, warning, danger)
     * @return Kết quả cập nhật từ ESP32
     */
    @PutMapping("/{id}/threshold")
    public ResponseEntity<ApiResponse<ThresholdResponse>> updateThreshold(
            @PathVariable Long id,
            @Valid @RequestBody ThresholdUpdateRequest request) {
        try {
            ThresholdResponse response = esp32Service.updateThreshold(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật ngưỡng thành công", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Không thể kết nối với ESP32: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy ngưỡng hiện tại từ ESP32
     * 
     * @param id ID của thiết bị
     * @return Thông tin ngưỡng hiện tại trên ESP32
     */
    @GetMapping("/{id}/threshold")
    public ResponseEntity<ApiResponse<ThresholdResponse>> getThreshold(@PathVariable Long id) {
        try {
            ThresholdResponse response = esp32Service.getThresholdFromDevice(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy ngưỡng thành công", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Không thể kết nối với ESP32: " + e.getMessage(), null));
        }
    }

    /**
     * Kiểm tra kết nối với ESP32
     * 
     * @param id ID của thiết bị
     * @return true nếu ESP32 đang online
     */
    @GetMapping("/{id}/health")
    public ResponseEntity<ApiResponse<Boolean>> checkDeviceHealth(@PathVariable Long id) {
        try {
            boolean isOnline = esp32Service.healthCheck(id);
            String message = isOnline ? "ESP32 đang hoạt động" : "ESP32 không phản hồi";
            return ResponseEntity.ok(ApiResponse.success(message, isOnline));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lỗi kiểm tra: " + e.getMessage(), false));
        }
    }

    /**
     * Cập nhật thông tin thiết bị
     * @param request Thông tin cập nhật thiết bị
     */
    @PutMapping("/update-info")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> updateDeviceInfo(
            @Valid @RequestBody UpdateDevice request) {
        try {
            DeviceResponseDto updatedDevice = deviceService.updateDevice(request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thiết bị thành công", updatedDevice));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Cập nhật thông tin thiết bị thất bại: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa thiết bị thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Xóa thiết bị thất bại: " + e.getMessage(), null));
        }
    }

    /**
     * Kích hoạt cảnh báo khẩn cấp - đưa ESP32 về trạng thái NGUY HIỂM
     * Ưu tiên sử dụng WebSocket, fallback về HTTP nếu device không online
     * @param id ID của thiết bị
     */
    @PostMapping("/{id}/alert/trigger")
    public ResponseEntity<ApiResponse<String>> triggerAlert(@PathVariable Long id) {
        try {
            // Lấy device để kiểm tra deviceCode
            var device = deviceService.getDeviceById(id);
            String deviceCode = device.getDeviceCode();
            
            // Thử gửi qua WebSocket trước
            if (webSocketHandler.isDeviceOnline(deviceCode)) {
                log.info("Device {} is online via WebSocket, sending alert via WebSocket", deviceCode);
                boolean sent = webSocketHandler.sendTriggerAlert(deviceCode);
                if (sent) {
                    return ResponseEntity.ok(ApiResponse.success(
                        "Kích hoạt cảnh báo khẩn cấp thành công qua WebSocket", 
                        "Alert sent via WebSocket"));
                }
            }
            
            // Fallback về HTTP nếu WebSocket không khả dụng
            log.info("Device {} not online via WebSocket, falling back to HTTP", deviceCode);
            String result = esp32Service.triggerEmergencyAlert(id);
            return ResponseEntity.ok(ApiResponse.success(
                "Kích hoạt cảnh báo khẩn cấp thành công qua HTTP", result));
                
        } catch (Exception e) {
            log.error("Failed to trigger alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Không thể kích hoạt cảnh báo: " + e.getMessage(), null));
        }
    }

    /**
     * Reset cảnh báo - đưa hệ thống về trạng thái ban đầu
     * Ưu tiên sử dụng WebSocket, fallback về HTTP nếu device không online
     * @param id ID của thiết bị
     */
    @PostMapping("/{id}/alert/reset")
    public ResponseEntity<ApiResponse<String>> resetAlert(@PathVariable Long id) {
        try {
            // Lấy device để kiểm tra deviceCode
            var device = deviceService.getDeviceById(id);
            String deviceCode = device.getDeviceCode();
            
            // Thử gửi qua WebSocket trước
            if (webSocketHandler.isDeviceOnline(deviceCode)) {
                log.info("Device {} is online via WebSocket, sending reset via WebSocket", deviceCode);
                boolean sent = webSocketHandler.sendResetAlert(deviceCode);
                if (sent) {
                    return ResponseEntity.ok(ApiResponse.success(
                        "Reset cảnh báo thành công qua WebSocket", 
                        "Reset sent via WebSocket"));
                }
            }
            
            // Fallback về HTTP nếu WebSocket không khả dụng
            log.info("Device {} not online via WebSocket, falling back to HTTP", deviceCode);
            String result = esp32Service.resetAlert(id);
            return ResponseEntity.ok(ApiResponse.success(
                "Reset cảnh báo thành công qua HTTP", result));
                
        } catch (Exception e) {
            log.error("Failed to reset alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Không thể reset cảnh báo: " + e.getMessage(), null));
        }
    }
}