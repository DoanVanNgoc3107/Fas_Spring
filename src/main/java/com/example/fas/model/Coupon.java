package com.example.fas.model;

import com.example.fas.model.enums.CouponStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon code cannot be blank")
    @Column(nullable = false, unique = true, length = 255)
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Coupon code must be uppercase alphanumeric without spaces")
    private String code;

    @NotBlank(message = "Description cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Discount percentage (0-100%)
    @NotNull(message = "Discount percentage cannot be null")
    @Min(value = 0, message = "Discount percentage must be at least 0%")
    @Max(value = 100, message = "Discount percentage cannot exceed 100%")
    private Integer discountPercentage;

    @Column(precision = 12, scale = 0)
    @DecimalMin(value = "0", message = "Min discount amount must be positive")
    private BigDecimal minAmountApply; // Số tiền tối thiểu áp dụng mã giảm giá

    // Maximum discount amount (VND)
    @Column(precision = 12, scale = 0)
    @DecimalMin(value = "0", message = "Max discount amount must be positive")
    private BigDecimal maxDiscountAmount;

    // Ngày bắt đầu áp dụng coupon
    @NotNull(message = "Start date cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant startAt;

    // Expiration date of the coupon
    @NotNull(message = "Expiration date cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant expireAt;

    // Status of the coupon (active/inactive)
    @Builder.Default
    @Column(nullable = false)
    private CouponStatus status = CouponStatus.ACTIVE;

    // Total quantity of coupons available
    @Min(value = 0)
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    // Number of times the coupon has been used
    @Builder.Default
    private Integer usedQuantity = 0;

    // One-to-many relationship with CouponHistory entity
    @OneToMany(mappedBy = "coupon", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private Set<CouponHistory> couponHistories = new HashSet<>();


    // Many-to-many relationship with Mail entity
    @JsonIgnore
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "coupon_mails", // bảng liên kết giữa Coupon và Mail
            joinColumns = @JoinColumn(name = "coupon_id"), // khóa ngoại tham chiếu đến Coupon
            inverseJoinColumns = @JoinColumn(name = "mail_id") // khóa ngoại tham chiếu đến Mail
    )
    private Set<Mail> mails = new HashSet<>();

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}