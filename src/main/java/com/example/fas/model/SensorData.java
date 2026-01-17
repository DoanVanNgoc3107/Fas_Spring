package com.example.fas.model;

import com.example.fas.model.enums.TypeSensor;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giá trị cảm biến gửi về (VD: nồng độ khói 300, 400...)
    private Double value;

    // Loại cảm biến (MQ2 hoặc DHT22)
    @Enumerated(EnumType.STRING)
    private TypeSensor typeSensor;

    // Thời điểm ghi nhận
    private Instant timestamp;

    // Dữ liệu này thuộc về thiết bị nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @PrePersist
    protected void onCreate() {
        timestamp = Instant.now();
    }
}