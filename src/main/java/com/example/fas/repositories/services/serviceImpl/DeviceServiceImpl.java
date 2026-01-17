package com.example.fas.repositories.services.serviceImpl;

import com.example.fas.mapper.dto.SensorDto.LatestSensorDataDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataRequestDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataResponseDto;
import com.example.fas.mapper.dto.deviceDto.RegisterDevice;
import com.example.fas.model.*;
import com.example.fas.model.enums.device.DeviceStatus;
import com.example.fas.model.enums.TypeSensor;
import com.example.fas.repositories.services.DeviceRepository;
import com.example.fas.repositories.services.SensorDataRepository;
import com.example.fas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import org.hibernate.annotations.Check;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl {

    private final DeviceRepository deviceRepository;
    private final SensorDataRepository sensorDataRepository;
    private final UserRepository userRepository;

    /**
     * Xử lý dữ liệu cảm biến nhận từ ESP32
     *
     * @param request Dữ liệu cảm biến từ ESP32
     */
    @Transactional
    public void processSensorData(SensorDataRequestDto request) {
        // 1. Tìm thiết bị dựa vào deviceCode gửi lên
        Device device = deviceRepository.findByDeviceCode(request.getDeviceCode())
                .orElseThrow(() -> new RuntimeException(
                        "Thiết bị không tồn tại: " + request.getDeviceCode()));

        // 2. Cập nhật thời gian hoạt động gần nhất (Heartbeat)
        device.setLastActiveTime(Instant.now());

        // 3. Lưu dữ liệu cảm biến vào bảng history (SensorData)
        SensorData sensorData = SensorData.builder()
                .device(device)
                .value(request.getValue())
                .typeSensor(request.getTypeSensor())
                .timestamp(Instant.now())
                .build();
        sensorDataRepository.save(sensorData);

        // 4. LOGIC BÁO CHÁY (Quan trọng)
        checkThresholdAndAlert(device, request);

        // 5. Lưu trạng thái thiết bị mới nhất
        deviceRepository.save(device);
    }

    // public boolean checkConnection(checkConnectDevideRequest request) {

    //         return true; // Thay thế bằng logic thực tế
    // }

    /**
     * Đăng ký thiết bị mới
     *
     * @param request Thông tin thiết bị cần đăng ký
     */
    @Transactional
    public void registerDevice(RegisterDevice request) {
        // Kiểm tra xem deviceCode đã tồn tại chưa
        if (deviceRepository.findByDeviceCode(request.getDeviceCode()).isPresent()) {
            throw new RuntimeException("Thiết bị với mã " + request.getDeviceCode() + " đã tồn tại");
        }

        // Tìm user theo userId
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + request.getUserId()));

        // Tạo thiết bị mới với các giá trị mặc định
        Device device = Device.builder()
                .deviceCode(request.getDeviceCode())
                .nameDevice(request.getDeviceName())
                .description(request.getDescription())
                .user(user)  // Gán user cho thiết bị
                .ipV4Address(request.getIpV4Address())
                .status(DeviceStatus.ACTIVE)
                .safetyThreshold(300.0) // Giá trị mặc định
                .warningThreshold(500.0) // Giá trị mặc định
                .dangerThreshold(700.0) // Giá trị mặc định
                .lastActiveTime(Instant.now())
                .build();

        deviceRepository.save(device);
    }

    /**
     * Kiểm tra trạng thái kết nối của thiết bị
     * Thiết bị được coi là OFFLINE nếu không gửi heartbeat trong vòng 5 phút
     *
     * @param deviceCode Mã thiết bị cần kiểm tra
     * @return true nếu thiết bị ONLINE, false nếu OFFLINE
     */
    public boolean checkDeviceConnection(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Thiết bị không tồn tại: " + deviceCode));

        // Kiểm tra xem lastActiveTime có null không
        if (device.getLastActiveTime() == null) {
            return false;
        }

        // Thiết bị được coi là OFFLINE nếu không gửi dữ liệu trong 5 phút
        Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
        return device.getLastActiveTime().isAfter(fiveMinutesAgo);
    }

    /**
     * Kiểm tra ngưỡng và phát cảnh báo nếu vượt mức
     *
     * @param device     Thiết bị
     * @param mq2Request Dữ liệu cảm biến MQ2
     */
    private void checkThresholdAndAlert(Device device, SensorDataRequestDto mq2Request) {
        // Chỉ check nếu là cảm biến Khói/Gas (MQ2)
        if (mq2Request.getTypeSensor() == TypeSensor.MQ2) {
            double value = mq2Request.getValue();
            Device app = deviceRepository.findByDeviceCode(device.getDeviceCode())
                    .orElseThrow(() -> new RuntimeException("Device not found"));

            // Cập nhật trạng thái thiết bị dựa trên ngưỡng
            // if (value > device.getDangerThreshold())
            //         app.setStatus(DeviceStatus.DANGER);
            // else if (value > device.getWarningThreshold())
            //         app.setStatus(DeviceStatus.WARNING);
            // else
            app.setStatus(DeviceStatus.ACTIVE);

            deviceRepository.save(app);
        }
    }

    // ==================== Methods cho NextJS API ====================

    /**
     * Lấy tất cả dữ liệu cảm biến theo deviceCode (có phân trang)
     */
    public Page<SensorDataResponseDto> getSensorDataByDeviceCode(String deviceCode, Pageable pageable) {
        Page<SensorData> sensorDataPage = sensorDataRepository.findByDeviceCode(deviceCode, pageable);
        return sensorDataPage.map(this::convertToResponseDto);
    }

    /**
     * Lấy dữ liệu theo deviceCode và loại cảm biến
     */
    public Page<SensorDataResponseDto> getSensorDataByDeviceCodeAndType(
            String deviceCode, TypeSensor typeSensor, Pageable pageable) {

        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Thiết bị không tồn tại: " + deviceCode));

        Page<SensorData> sensorDataPage = sensorDataRepository
                .findByDeviceIdAndTypeSensorOrderByTimestampDesc(device.getId(), typeSensor, pageable);

        return sensorDataPage.map(this::convertToResponseDto);
    }

    /**
     * Lấy dữ liệu mới nhất (cho dashboard real-time)
     * @param deviceCode Mã thiết bị
     * @return LatestSensorDataDto chứa dữ liệu mới nhất
     */
    public LatestSensorDataDto getLatestSensorData(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Thiết bị không tồn tại: " + deviceCode));

        // Lấy dữ liệu MQ2 mới nhất
        SensorData latestMq2 = sensorDataRepository
                .findFirstByDeviceIdAndTypeSensorOrderByTimestampDesc(device.getId(), TypeSensor.MQ2);

        // Lấy dữ liệu DHT22 mới nhất (nếu có)
        SensorData latestDht22 = sensorDataRepository
                .findFirstByDeviceIdAndTypeSensorOrderByTimestampDesc(device.getId(), TypeSensor.DHT22);

        return LatestSensorDataDto.builder()
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getNameDevice())
                .mq2Value(latestMq2 != null ? latestMq2.getValue() : null)
                .mq2Timestamp(latestMq2 != null ? latestMq2.getTimestamp() : null)
                .deviceStatus(device.getStatus() != null ? device.getStatus().name() : "UNKNOWN")
                .lastActiveTime(device.getLastActiveTime())
                .build();
    }

    /**
     * Lấy dữ liệu trong khoảng thời gian
     */
    public List<SensorDataResponseDto> getSensorDataByTimeRange(
            String deviceCode, Instant startTime, Instant endTime) {

        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Thiết bị không tồn tại: " + deviceCode));

        List<SensorData> sensorDataList = sensorDataRepository
                .findByDeviceIdAndTimestampBetween(device.getId(), startTime, endTime);

        return sensorDataList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy 10 bản ghi mới nhất
     */
    public List<SensorDataResponseDto> getRecentSensorData(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Thiết bị không tồn tại: " + deviceCode));

        List<SensorData> sensorDataList = sensorDataRepository
                .findTop10ByDeviceIdOrderByTimestampDesc(device.getId());

        return sensorDataList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method: Convert Entity sang DTO
     */
    private SensorDataResponseDto convertToResponseDto(SensorData sensorData) {
        return SensorDataResponseDto.builder()
                .id(sensorData.getId())
                .deviceCode(sensorData.getDevice().getDeviceCode())
                .deviceName(sensorData.getDevice().getNameDevice())
                .value(sensorData.getValue())
                .typeSensor(sensorData.getTypeSensor())
                .timestamp(sensorData.getTimestamp())
                .build();
    }
}