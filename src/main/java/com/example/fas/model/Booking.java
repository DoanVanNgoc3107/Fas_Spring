package com.example.fas.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @OneToOne(optional = false) // Each booking has one payment
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // User who made the booking
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Room price per day cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal ratePerDay;

    // Time when the booking starts and ends
    private Instant startAt;
    private Instant endAt;

    // PaymentStatus details
    private Instant paidAt;
    private Instant expireAt;
    
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        // Validate dates
        if (endAt != null && startAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        // Auto-calculate total amount if not set
        if (totalAmount == null && room != null && room.getPrice() != null && startAt != null && endAt != null) {
            long days = java.time.Duration.between(startAt, endAt).toDays();
            if (days == 0) days = 1; // Minimum 1 day
            totalAmount = room.getPrice().multiply(BigDecimal.valueOf(days));
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    } 
}
