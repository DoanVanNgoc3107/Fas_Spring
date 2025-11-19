package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "banks")
@Data
@Getter
@Setter
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the bank name of user
    @NotBlank(message = "Bank name cannot be blank")
    @Column(nullable = false)
    private String bankName;

    // the bank account number of user
    @NotNull(message = "Bank number cannot be null")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "Account number must be between 8 and 20 digits")
    @Column(nullable = false)
    private String accountNumber;

    @NotBlank(message = "Owner bank account cannot be blank")
    private String ownerBankAccount;

    // the bank account holder name of user
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
