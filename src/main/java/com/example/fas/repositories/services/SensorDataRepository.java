package com.example.fas.repositories.services;

import com.example.fas.model.SensorData;
import com.example.fas.model.enums.TypeSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    
    // Lấy tất cả dữ liệu của một thiết bị (có phân trang)
    Page<SensorData> findByDeviceIdOrderByTimestampDesc(Long deviceId, Pageable pageable);
    
    // Lấy dữ liệu của một thiết bị theo loại cảm biến (có phân trang)
    Page<SensorData> findByDeviceIdAndTypeSensorOrderByTimestampDesc(
            Long deviceId, TypeSensor typeSensor, Pageable pageable);
    
    // Lấy dữ liệu trong khoảng thời gian
    @Query("SELECT s FROM SensorData s WHERE s.device.id = :deviceId " +
           "AND s.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY s.timestamp DESC")
    List<SensorData> findByDeviceIdAndTimestampBetween(
            @Param("deviceId") Long deviceId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    // Lấy timestamp mới nhất của dữ liệu cảm biến của thiết bị
    @Query("SELECT MAX(s.timestamp) FROM SensorData s WHERE s.device.id = :deviceId")
    Instant findLatestTimestampByDeviceId(@Param("deviceId") Long deviceId);
    
    // Lấy dữ liệu mới nhất của một thiết bị theo loại cảm biến
    SensorData findFirstByDeviceIdAndTypeSensorOrderByTimestampDesc(
            Long deviceId, TypeSensor typeSensor);
    
    // Lấy dữ liệu mới nhất của một thiết bị (bất kỳ loại cảm biến)
    SensorData findFirstByDeviceIdOrderByTimestampDesc(Long deviceId);
    
    // Lấy N bản ghi gần nhất của một thiết bị
    List<SensorData> findTop10ByDeviceIdOrderByTimestampDesc(Long deviceId);
    
    // Lấy tất cả dữ liệu theo deviceCode (dùng để NextJS query)
    @Query("SELECT s FROM SensorData s WHERE s.device.deviceCode = :deviceCode " +
           "ORDER BY s.timestamp DESC")
    Page<SensorData> findByDeviceCode(@Param("deviceCode") String deviceCode, Pageable pageable);
}