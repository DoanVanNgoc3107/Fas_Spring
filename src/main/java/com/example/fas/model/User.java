package com.example.fas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
        ACTIVE, PENDING, DELETED
    }

    public enum Role {
        ADMIN, MANAGER, RESIDENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name can not be blank")
    @Size(min = 3, message = "Full name must be more than 3 characters")
    private String fullName;

    @NotBlank(message = "Username can not be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password can not be blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(unique = true, length = 10)
    @NotBlank(message = "Identity card can not be blank")
    private String identityCard;

    @Column(unique = true, length = 12)
    @Pattern(regexp = "^\\d{12}$", message = "Citizen ID (CCCD) must be exactly 12 digits")
    private String citizenId;

    @Pattern(regexp = "^(\\+84|0)(3[2-9]|5[689]|7[0-9]|8[1-5]|9[0-46-9])[0-9]{7}$"
            , message = "Invalid phone number")
    private String phoneNumber;

    @NotNull(message = "Creation timestamp cannot be null")
    private Instant createdAt;

    @NotNull(message = "Update timestamp cannot be null")
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

    public static String generateRandomAlphanumericIdentityCard() {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 10; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }
}
