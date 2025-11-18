package com.example.fas.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.example.fas.enums.room.RoomStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Room name
    @NotBlank(message = "Room name cannot be blank")
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String roomName;

    // Room description
    @NotBlank(message = "Room description cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Unique room code
    @NotNull(message = "Room code cannot be null")
    @Size(min = 3, message = "Room code must be between 3 characters")
    @Column(nullable = false, unique = true)
    private String roomCode;

    // Address of the room
    @NotBlank(message = "Address cannot be blank")
    @Column(columnDefinition = "TEXT")
    private String address;

    // List of image URLs for the room
    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url", nullable = false)
    private List<@NotBlank(message = "Image URL cannot be blank") String> imageURL;

    // Area of the room in square meters (m^2)
    @NotNull(message = "Area cannot be null")
    @Pattern(regexp = "^[0-9]+$", message = "Area must be a valid number")
    private Integer area;

    // Price of the room in the local currency (inclusive: bao gồm cả 0)
    // Validate cho tiền tệ việt nam đồng (VND) - không có phần thập phân
    @Builder.Default
    @Column(precision = 12, scale = 0) // precision: tổng số chữ số, scale: số chữ số thập phân
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0", inclusive = true, message = "Price must be greater than 0")
    @Digits(integer = 12, fraction = 0, message = "Price must be a valid monetary amount")
    private BigDecimal price = BigDecimal.ZERO;

    // Room availability status (AVAILABLE, OCCUPIED, RESERVED, CLEANING, MAINTENANCE, OUT_OF_SERVICE)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Availability userStatus cannot be null")
    private RoomStatus isAvailable;

    // Many-to-one relationship with Landlord
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @NotNull(message = "Landlord cannot be null")
    @ToString.Exclude
    private Landlord landlord;


    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Appreciate> appreciates;

    // Many-to-one relationship with User (tenant)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User tenant;

    // One-to-many relationship with Bookings
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Set<Booking> bookings = new java.util.HashSet<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC+7")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC+7")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
