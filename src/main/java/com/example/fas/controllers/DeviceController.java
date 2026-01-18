package com.example.fas.controllers;

import com.example.fas.mapper.dto.SensorDto.LatestSensorDataDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataRequestDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataResponseDto;
import com.example.fas.mapper.dto.deviceDto.DeviceResponseDto;
import com.example.fas.mapper.dto.deviceDto.RegisterDevice;
import com.example.fas.model.ApiResponse;
import com.example.fas.model.enums.TypeSensor;
import com.example.fas.repositories.services.serviceImpl.DeviceServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceServiceImpl deviceService;

    /**
     * Kiểm tra trạng thái kết nối của thiết bị
     *
     * @param deviceCode Mã thiết bị
     */
    @PostMapping("/test-connection/{deviceCode}")
    public ResponseEntity<ApiResponse<String>> testConnection(@Valid @PathVariable String deviceCode) {
        return ResponseEntity.ok(ApiResponse.success("Kết nối thành công", null));
    }

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
                    null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lấy thiết bị thất bại: " + e.getMessage(), null));
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
     *
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
}