package com.example.fas.repositories.services;

import com.example.fas.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceCode(String deviceCode);
    
    List<Device> findByUserId(Long userId);

    boolean existsByDeviceCode(String deviceCode);

    boolean existsByIpV4Address(String ipV4Address);
}