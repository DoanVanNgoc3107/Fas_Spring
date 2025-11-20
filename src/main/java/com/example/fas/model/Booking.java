package com.example.fas.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit; // Dùng cái này để cộng thời gian
import java.util.HashSet;
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

    // Mối quan hệ với Room (thanh toán cho phòng nào)
    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Mối quan hệ với Payment (thông tin thanh toán)
    @ToString.Exclude
    @JsonIgnore
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    // Mối quan hệ với Appreciate (đánh giá sau khi sử dụng dịch vụ)
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private Appreciate appreciate;

    // User nào đã thực hiện booking
    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    // Lịch sử sử dụng coupon cho booking này
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private Set<CouponHistory> couponHistories = new HashSet<>();

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

    // Giá phòng tại thời điểm booking (snapshot)
    @NotNull(message = "Room price per day cannot be null")
    private BigDecimal ratePerDay; // Lưu giá phòng tại thời điểm booking

    // Thời điểm thanh toán thành công (có thể null nếu chưa trả)
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant paidAt; // Thời điểm thanh toán thành công

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
            long days = Duration.between(startAt, endAt).toDays(); // Số ngày nguyên
            long extraHours = Duration.between(startAt, endAt).toHoursPart(); // Phần giờ lẻ

            if (extraHours > 0) {
                days += 1; // Làm tròn lên nếu có giờ lẻ
            }

            if (days == 0) {
                days = 1; // Ít nhất 1 ngày
            }

            // Lấy giá phòng từ entity Room (hoặc giá snapshot ratePerDay)
            BigDecimal price = this.ratePerDay != null ? this.ratePerDay : this.room.getPrice();

            this.totalAmount = price.multiply(BigDecimal.valueOf(days));
        }
    }
}