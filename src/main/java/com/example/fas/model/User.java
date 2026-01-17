package com.example.fas.model;

import com.example.fas.model.enums.oauth2.AuthProvider;
import com.example.fas.model.enums.user.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "`users`")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 3, message = "Full name must be more than 3 characters")
    private String fullName;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = true)
    private String password;

    @NotNull(message = "UserStatus cannot be null")
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @NotNull(message = "AuthProvider provider cannot be null")
    private AuthProvider provider = AuthProvider.NONE;

    private String providerId;

    private String avatarUrl;

    // Mỗi user có thể chứa nhiều thiết bị
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<Device> devices = new HashSet<>();

    // Timestamp of creation
    @NotNull(message = "Creation timestamp cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;

    // Timestamp of the last update
    @NotNull(message = "Update timestamp cannot be null")
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
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

    // Ghi đè phương thức equals để so sánh hai đối tượng User dựa trên id.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return Objects.equals(id, user.id);
    }

    // Ghi đè phương thức hashCode để trả về mã băm dựa trên id,
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
