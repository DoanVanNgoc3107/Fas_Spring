package com.example.fas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "rooms")
@Data
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room name cannot be blank")
    private String roomName;

    @NotBlank(message = "Room description cannot be blank")
    private String description;
    
    @NotNull(message = "Room code cannot be null")
    private Integer roomCode;

    @NotBlank(message = "Image URL cannot be blank")
    private String imageURL;

    @NotNull(message = "Availability status cannot be null")
    private Boolean isAvailable;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
