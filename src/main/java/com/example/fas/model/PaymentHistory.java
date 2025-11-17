package com.example.fas.model;

import com.example.fas.enums.payment.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_histories")
@Data
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Amount paid (VND)
    @Column(nullable = false)
    private BigDecimal amount;

    // User who made the payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Associated payment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // Status of the payment
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // Description of the payment
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    // Additional notes
    @Column(nullable = false)
    private String note;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }
}
