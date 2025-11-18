package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "landlords")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Landlord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the landlord (họ và tên chủ nhà)
    @NotBlank(message = "Landlord name cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String nameLandlord;

    // Description of the landlord (mô tả về chủ nhà)
    @NotBlank(message = "Landlord description cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descriptionLandlord;

    // Contact information of the landlord (thông tin liên hệ chủ nhà)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One-to-many relationship with Room entity
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> rooms = new HashSet<>();

    // Danh sách ưa thích chủ nhà bởi người dùng
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "favoriteLandlord", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @NotNull(message = "CreatedAt cannot be null")
    private Instant createdAt;

    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @NotNull(message = "UpdatedAt cannot be null")
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

    /**
     * Ghi đè phương thức equals để so sánh hai đối tượng Landlord dựa trên id.
     *
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Landlord landlord = (Landlord) o;

        return Objects.equals(id, landlord.id);
    }

    /**
     * Ghi đè phương thức hashCode để trả về mã băm dựa trên id,
     * giúp duy trì tính nhất quán với phương thức equals.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
