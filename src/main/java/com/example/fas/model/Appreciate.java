package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "appreciates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appreciate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Content of the appreciation/review
    @NotBlank(message = "Content cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Rating from 1 to 5
    @NotNull(message = "Rating cannot be null")
    @Min(value =0, message = "Rating must be at least 0")
    @Max(value = 10, message = "Rating must be at most 10")
    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(nullable = false)
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
