package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "`users`")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public enum Status {
        ACTIVE, BLOCKED, DELETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String gender;
    private String birthday;
    private String nationality;
    private String identityCard;
    private String email;
    private String code;
    private String username;
    private String password;
    private String avatar;
    private String phoneNumber;
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Status status;

    private Instant create_at;
    private Instant update_at;
}
