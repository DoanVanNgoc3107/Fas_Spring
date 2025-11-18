package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

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

    // Expiration date of the coupon
    @NotNull(message = "Expiration date cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant expireAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Min(value = 0)
    private Integer quantity;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}