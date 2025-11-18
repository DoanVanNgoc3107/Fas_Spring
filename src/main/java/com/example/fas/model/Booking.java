package com.example.fas.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit; // Dùng cái này để cộng thời gian
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ToString.Exclude
    @JsonIgnore
    @OneToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @OneToMany(mappedBy = "booking",fetch =  FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private Set<Coupon> coupons;

    // --- 1. THÔNG TIN LƯU TRÚ (Dùng để tính tiền & Check lịch) ---

    @NotNull(message = "Start date cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant startAt; // Check-in

    @NotNull(message = "End date cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant endAt;   // Check-out

    // --- 2. THÔNG TIN THANH TOÁN (Dùng để xử lý giao dịch) ---
    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Room price per day cannot be null")
    private BigDecimal ratePerDay;

    // Thời điểm thanh toán thành công (có thể null nếu chưa trả)
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant paidAt;

    // Hạn chót thanh toán (Booking sẽ bị hủy nếu quá giờ này mà chưa paidAt)
    // Ví dụ: Payment Window = 15 phút
    @NotNull(message = "Expiration date cannot be null")
    private Instant expireAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Tự động set hạn thanh toán là 15 phút sau khi tạo đơn
        if (this.expireAt == null) {
            this.expireAt = now.plus(15, ChronoUnit.MINUTES);
        }
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Logic tính tiền CHUẨN:
     * Dựa vào startAt (Check-in) và endAt (Check-out)
     */
    public void calculateTotalAmount() {
        if (this.startAt != null && this.endAt != null && this.room != null) {
            // Tính số ngày (làm tròn lên)
            long days = Duration.between(startAt, endAt).toDays();

            // Logic bổ sung: Nếu ở chưa đến 24h nhưng qua đêm thì vẫn tính 1 ngày
            if (days < 1) days = 1;

            // Lấy giá phòng từ entity Room (hoặc giá snapshot ratePerDay)
            BigDecimal price = this.ratePerDay != null ? this.ratePerDay : this.room.getPrice();

            this.totalAmount = price.multiply(BigDecimal.valueOf(days));
        }
    }
}