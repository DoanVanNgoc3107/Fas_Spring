package com.example.fas.config;

import com.example.fas.model.Device;
import com.example.fas.model.enums.device.DeviceStatus;
import com.example.fas.repositories.services.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceStatusScheduler {

    private final DeviceRepository deviceRepository;

    // Chạy mỗi 10 giây để kiểm tra trạng thái thiết bị
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkDeviceStatus() {
        List<Device> devices = deviceRepository.findAll();
        Instant now = Instant.now();

        for (Device device : devices) {
            Instant lastActive = device.getLastActiveTime();

            // Nếu quá 30 giây không nhận được tín hiệu => OFFLINE
            if (lastActive == null || now.isAfter(lastActive.plusSeconds(30))) {
                if (device.getStatus() != DeviceStatus.OFFLINE) {
                    device.setStatus(DeviceStatus.OFFLINE);
                    deviceRepository.save(device);
                    log.info("Device {} changed to OFFLINE", device.getDeviceCode());
                }
            }
        }
    }
}