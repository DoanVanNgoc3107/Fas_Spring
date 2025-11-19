package com.example.fas.model;

import com.example.fas.enums.bookingService.TypeBookingService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "standards")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name service cannot be blank")
    @Column(nullable = false, unique = true) // Tên dịch vụ không được để trống và phải là duy nhất
    private String nameService;

    // Mô tả dịch vụ
    @NotBlank(message = "Description cannot be blank")
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "Unit cannot be blank")
    @Column(nullable = false) // Đơn vị tính không được để trống
    private String unit;

    @NotNull(message = "Price cannot be null")
    @Column(nullable = false) // Giá không được để trống
    @DecimalMin(value = "0", message = "Price must be non-negative")
    @Digits(integer = 20, fraction = 0, message = "Balance must be a valid monetary amount (VND)")
    private BigDecimal price = BigDecimal.ZERO;

    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "bookingServices", fetch = FetchType.LAZY)
    private Set<Room> rooms;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    @Builder.Default
    private TypeBookingService status = TypeBookingService.STANDARD;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
