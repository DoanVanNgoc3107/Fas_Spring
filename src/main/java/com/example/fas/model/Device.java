package com.example.fas.model;

import com.example.fas.model.enums.device.DeviceStatus;
import com.example.fas.model.enums.room.RoomType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotNull(message = "Device code cannot be null")
    private String deviceCode; // Mã thiết bị (ESP32 gửi lên)

    private String nameDevice;

    private String description;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private RoomType room;

    @NotBlank(message = "IP address cannot be blank")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "Invalid IPv4 address format")
    private String ipV4Address;

    // Người sở hữu thiết bị
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 2. Ngưỡng an toàn/cảnh báo/nguy cấp cho cảm biến khói (MQ2)
    private Double safetyThreshold; // Ngưỡng an toàn

    private Double warningThreshold; // Ngưỡng cảnh báo

    private Double dangerThreshold; // Ngưỡng nguy cấp

    // Trạng thái của thiết bị esp32 (ONLINE, OFFLINE)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private DeviceStatus status;

    // Dùng để phát hiện thiết bị bị mất điện hoặc mất Wifi
    private Instant lastActiveTime;

    @JsonIgnore
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private List<SensorData> sensorDataList;
}