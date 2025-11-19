package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Event name cannot be blank")
    @Column(nullable = false)
    private String eventName;

    @NotBlank(message = "Event description cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventDescription;

    @NotBlank(message = "Poster URL path cannot be blank")
    @Column(nullable = false)
    private String posterUrlPath;

    @NotBlank(message = "Event URL path cannot be blank")
    @Column(nullable = false)
    private String eventUrlPath;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant startAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant endAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (this.startAt == null) this.startAt = now;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
