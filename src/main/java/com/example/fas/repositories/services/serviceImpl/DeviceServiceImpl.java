package com.example.fas.repositories.services.serviceImpl;

import com.example.fas.mapper.dto.SensorDto.LatestSensorDataDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataRequestDto;
import com.example.fas.mapper.dto.SensorDto.SensorDataResponseDto;
import com.example.fas.mapper.dto.deviceDto.UpdateDevice;
import com.example.fas.mapper.dto.deviceDto.DeviceResponseDto;
import com.example.fas.mapper.dto.deviceDto.RegisterDevice;
import com.example.fas.model.*;
import com.example.fas.model.enums.device.DeviceStatus;
import com.example.fas.model.enums.TypeSensor;
import com.example.fas.repositories.services.DeviceRepository;
import com.example.fas.repositories.services.SensorDataRepository;
import com.example.fas.repositories.UserRepository;
import com.example.fas.repositories.services.serviceImpl.exceptions.device.DeviceCodeExistException;
import com.example.fas.repositories.services.serviceImpl.exceptions.device.IdDeviceNotFoundException;
import com.example.fas.repositories.services.serviceImpl.exceptions.general.notfound.IdNotFoundException;
import com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound.UserIDNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl {

    private final DeviceRepository deviceRepository;
    private final SensorDataRepository sensorDataRepository;
    private final UserRepository userRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository,
            SensorDataRepository sensorDataRepository,
            UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorDataRepository = sensorDataRepository;
        this.userRepository = userRepository;
    }

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
        device.setStatus(DeviceStatus.ACTIVE);

        // 2.1. Cập nhật địa chỉ IP của ESP32 nếu có (quan trọng cho two-way communication)
        if (request.getIpV4Address() != null && !request.getIpV4Address().trim().isEmpty()) {
            device.setIpV4Address(request.getIpV4Address().trim());
        }

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

    // Cập nhật trạng thái của esp32 nếu không nhận được dữ liệu trong vòng 30 giây
    @Transactional
    public void updateDeviceStatus(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IdDeviceNotFoundException("Thiết bị không tồn tại: " + id));

        Instant latestTimestamp = sensorDataRepository.findLatestTimestampByDeviceId(id);
        Instant now = Instant.now();

        // Nếu chưa từng có dữ liệu hoặc quá 30s không nhận được dữ liệu mới => OFFLINE
        if (latestTimestamp == null || now.isAfter(latestTimestamp.plusSeconds(30))) {
            device.setStatus(DeviceStatus.OFFLINE);
        } else {
            device.setStatus(DeviceStatus.ACTIVE);
        }

        deviceRepository.save(device);
    }

    /**
     * Đăng ký thiết bị mới
     *
     * @param request Thông tin thiết bị cần đăng ký
     */
    @Transactional
    public void registerDevice(RegisterDevice request) {
        // Kiểm tra xem deviceCode đã tồn tại chưa
        if (deviceRepository.findByDeviceCode(request.getDeviceCode()).isPresent()) {
            throw new DeviceCodeExistException("Thiết bị với mã " + request.getDeviceCode() + " đã tồn tại");
        }

        if (request.getIpV4Address() == null || request.getIpV4Address().isEmpty()) {
            throw new RuntimeException("Địa chỉ IP " + request.getIpV4Address() + " không hợp lệ hoặc đã được sử dụng");
        }

        // Tìm user theo userId
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserIDNotFoundException("Không tìm thấy user với ID: " + request.getUserId()));

        // Tạo thiết bị mới với các giá trị mặc định
        Device device = Device.builder()
                .deviceCode(request.getDeviceCode())
                .nameDevice(request.getDeviceName())
                .description(request.getDescription())
                .user(user)
                .ipV4Address(request.getIpV4Address())
                .status(DeviceStatus.ACTIVE)
                .safetyThreshold(300.0)
                .warningThreshold(500.0)
                .dangerThreshold(700.0)
                .lastActiveTime(Instant.now())
                .build();

        Set<Device> listDevice = user.getDevices();
        listDevice.add(device);
        user.setDevices(listDevice);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DeviceCodeExistException("Thiết bị với mã " + request.getDeviceCode() + " đã tồn tại");
        }
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
            Device app = deviceRepository.findByDeviceCode(device.getDeviceCode())
                    .orElseThrow(() -> new IdDeviceNotFoundException("Device not found"));
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

    // Get all device
    public List<DeviceResponseDto> getAllDevices() {
        List<Device> devices = deviceRepository.findAll();
        return devices.stream()
                .map(this::convertToDeviceResponseDto)
                .collect(Collectors.toList());
    }

    public List<DeviceResponseDto> getDevicesByUserId(Long userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        return devices.stream()
                .map(this::convertToDeviceResponseDto)
                .collect(Collectors.toList());
    }

    // ==================== Private Helpers ====================

    private DeviceResponseDto convertToDeviceResponseDto(Device device) {
        return DeviceResponseDto.builder()
                .id(device.getId())
                .ownerId(device.getUser().getId())
                .deviceCode(device.getDeviceCode())
                .nameDevice(device.getNameDevice())
                .room(device.getRoom() != null ? String.valueOf(device.getRoom()) : null)
                .description(device.getDescription())
                .status(device.getStatus().name())
                .ipV4Address(device.getIpV4Address())
                .safetyThreshold(device.getSafetyThreshold())
                .warningThreshold(device.getWarningThreshold())
                .dangerThreshold(device.getDangerThreshold())
                .lastActiveTime(device.getLastActiveTime() != null ? device.getLastActiveTime().toString() : null)
                .build();
    }

    /**
     * Lấy thông tin thiết bị theo ID
     * 
     * @param id ID thiết bị
     */
    public DeviceResponseDto getDeviceById(Long id) {
        if (id <= 0) {
            throw new IdNotFoundException("Id của thiết bị không hợp lệ: " + id);
        }

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Không tìm thấy thiết bị với id: " + id));

        return convertToDeviceResponseDto(device);
    }

    public DeviceResponseDto getDeviceByCode(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new IdDeviceNotFoundException("Không tìm thấy thiết bị với mã: " + deviceCode));

        return convertToDeviceResponseDto(device);
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
     *
     * @param id Mã thiết bị
     * @return LatestSensorDataDto chứa dữ liệu mới nhất
     */
    public LatestSensorDataDto getLatestSensorData(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thiết bị không tồn tại: " + id));

        // Lấy dữ liệu MQ2 mới nhất
        SensorData latestMq2 = sensorDataRepository
                .findFirstByDeviceIdAndTypeSensorOrderByTimestampDesc(device.getId(), TypeSensor.MQ2);

        // Lấy dữ liệu DHT22 mới nhất (nếu có)
        sensorDataRepository.findFirstByDeviceIdAndTypeSensorOrderByTimestampDesc(device.getId(), TypeSensor.DHT22);

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
     * Lấy dữ liệu trong khoảng thời gian dựa trên id
     */
    public List<SensorDataResponseDto> getSensorDataByTimeRange(
            Long id, Instant startTime, Instant endTime) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IdDeviceNotFoundException("Thiết bị không tồn tại: " + id));

        List<SensorData> sensorDataList = sensorDataRepository
                .findByDeviceIdAndTimestampBetween(device.getId(), startTime, endTime);

        return sensorDataList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy 10 bản ghi mới nhất
     */
    public List<SensorDataResponseDto> getRecentSensorData(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thiết bị không tồn tại: " + id));

        List<SensorData> sensorDataList = sensorDataRepository
                .findTop10ByDeviceIdOrderByTimestampDesc(device.getId());

        return sensorDataList.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin thiết bị
     * 
     * @param updateDevice DTO chứa thông tin cập nhật
     * @return DeviceResponseDto sau khi cập nhật
     */
    @Transactional
    public DeviceResponseDto updateDevice(UpdateDevice updateDevice) {
        // Validate ID
        if (updateDevice.getId() == null || updateDevice.getId() <= 0) {
            throw new IdDeviceNotFoundException("Id của thiết bị không hợp lệ: " + updateDevice.getId());
        }

        // Tìm thiết bị
        Device device = deviceRepository.findById(updateDevice.getId())
                .orElseThrow(
                        () -> new IdDeviceNotFoundException("Không tìm thấy thiết bị với id: " + updateDevice.getId()));

        // Cập nhật thông tin cơ bản (chỉ khi không null và không rỗng)
        if (updateDevice.getDeviceName() != null && !updateDevice.getDeviceName().trim().isEmpty()) {
            device.setNameDevice(updateDevice.getDeviceName().trim());
        }
        
        if (updateDevice.getIpv4Address() != null && !updateDevice.getIpv4Address().trim().isEmpty()) {
            // Validate IP format (optional - có thể thêm regex validation)
            device.setIpV4Address(updateDevice.getIpv4Address().trim());
        }
        
        if (updateDevice.getDescription() != null && !updateDevice.getDescription().trim().isEmpty()) {
            device.setDescription(updateDevice.getDescription().trim());
        }

        // Cập nhật ngưỡng với validation
        Double safety = updateDevice.getSafetyThreshold();
        Double warning = updateDevice.getWarningThreshold();
        Double danger = updateDevice.getDangerThreshold();

        // Nếu có cập nhật ngưỡng, validate logic tăng dần
        if (safety != null || warning != null || danger != null) {
            // Lấy giá trị hiện tại nếu không cập nhật
            Double currentSafety = safety != null ? safety : device.getSafetyThreshold();
            Double currentWarning = warning != null ? warning : device.getWarningThreshold();
            Double currentDanger = danger != null ? danger : device.getDangerThreshold();

            // Validate: An toàn < Cảnh báo < Nguy hiểm
            if (currentSafety != null && currentWarning != null && currentDanger != null) {
                if (!(currentSafety < currentWarning && currentWarning < currentDanger)) {
                    throw new IllegalArgumentException(
                            String.format("Ngưỡng không hợp lệ. Phải thỏa: An toàn (%.1f) < Cảnh báo (%.1f) < Nguy hiểm (%.1f)",
                                    currentSafety, currentWarning, currentDanger));
                }
            }

            // Cập nhật các ngưỡng
            if (safety != null) {
                if (safety < 0) {
                    throw new IllegalArgumentException("Ngưỡng an toàn phải >= 0");
                }
                device.setSafetyThreshold(safety);
            }
            if (warning != null) {
                if (warning < 0) {
                    throw new IllegalArgumentException("Ngưỡng cảnh báo phải >= 0");
                }
                device.setWarningThreshold(warning);
            }
            if (danger != null) {
                if (danger < 0) {
                    throw new IllegalArgumentException("Ngưỡng nguy hiểm phải >= 0");
                }
                device.setDangerThreshold(danger);
            }
        }

        // Lưu và trả về
        Device updatedDevice = deviceRepository.save(device);
        return convertToDeviceResponseDto(updatedDevice);
    }

    /**
     * Xóa thiết bị theo ID
     * 
     * @param id ID thiết bị cần xóa
     */
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IdDeviceNotFoundException("Không tìm thấy thiết bị với id: " + id));
        deviceRepository.delete(device);
    }

    /**
     * Helper method: Convert Entity sang DTO
     * 
     * @param sensorData Entity SensorData
     * @return SensorDataResponseDto
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
