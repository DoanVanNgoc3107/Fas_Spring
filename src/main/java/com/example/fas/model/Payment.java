package com.example.fas.model;

import com.example.fas.model.enums.payment.PaymentMethod;
import com.example.fas.model.enums.payment.PaymentProvider;
import com.example.fas.model.enums.payment.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Payment amount (VND)
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    // Method of payment (e.g., CREDIT_CARD, DEBIT_CARD, etc.)
    @NotNull(message = "Payment methods cannot be null")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    // Payment provider (e.g., MOMO, VNPAY, etc.)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "PaymentStatus provider cannot be null")
    private PaymentProvider paymentProvider;

    // Payment userStatus (e.g., SUCCESS, FAILED, PENDING)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "PaymentStatus cannot be null")
    private PaymentStatus paymentStatus;

    // ID of the user who made the payment
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One-to-one relationship with Booking
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    // One-to-one relationship with PaymentHistory
    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, optional = false)
    private PaymentHistory paymentHistory;

    @NotNull(message = "Payment date cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant paymentDate;

    @PrePersist
    public void onCreat() {
        this.paymentDate = Instant.now();
    }
}
