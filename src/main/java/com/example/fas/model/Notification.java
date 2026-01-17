package com.example.fas.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;   // VD: "CẢNH BÁO KHÓI!"
    private String message; // VD: "Phát hiện nồng độ khói cao tại Phòng 101"

    private boolean isRead; // Đã xem chưa

    // Thông báo này gửi cho ai? (Lấy từ user_id của cái Phòng bị cháy)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        isRead = false;
    }
}