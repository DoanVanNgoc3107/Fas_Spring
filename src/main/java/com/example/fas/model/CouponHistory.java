package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "coupon_histories", uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id"}))
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CouponHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nếu thanh toán thành công với mã coupon thì lưu vào bảng này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Nếu thanh toán thành công với mã coupon thì lưu vào bảng này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    // Người dùng sử dụng mã coupon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Thời gian sử dụng mã coupon
    @NotNull(message = "UsedAt cannot be null")
    @Column(name = "used_at")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant usedAt;

    @PrePersist
    public void onUsedAt() {
        this.usedAt = Instant.now();
    }
}
