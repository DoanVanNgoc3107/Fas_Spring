package com.example.fas.model;

import com.example.fas.enums.oauth2.AuthProvider;
import com.example.fas.enums.user.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
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
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @NotNull(message = "AuthProvider provider cannot be null")
    private AuthProvider provider = AuthProvider.NONE;

    private String providerId;

    private String avatarUrl;

    // User's account balance (VND)
    @Column(precision = 20, scale = 0) // precision: tổng số chữ số, scale: số chữ số thập phân
    @Builder.Default
    @NotNull(message = "Balance cannot be null (VND)")
    @DecimalMin(value = "0", message = "Balance must be non-negative (VND)")
    @Digits(integer = 20, fraction = 0, message = "Balance must be a valid monetary amount (VND)")
    private BigDecimal balance = BigDecimal.ZERO;

    // Identity card number (exactly 12 digits)
    @Column(unique = true, length = 12)
    @Pattern(regexp = "^\\d{12}$", message = "Identity card must be exactly 12 digits")
    private String identityCard;

    // Phone number (Vietnamese format)
    @Column(unique = true)
    @Pattern(regexp = "^(\\+84|0)(3[2-9]|5[689]|7[0-9]|8[1-5]|9[0-46-9])[0-9]{7}$", message = "Invalid phone number")
    private String phoneNumber;

    // Một đối một với thông tin ngân hàng của người dùng
    // Khi xóa người dùng, thông tin ngân hàng cũng bị xóa (orphanRemoval = true)
    @OneToOne(mappedBy = "user", orphanRemoval = true)
    @ToString.Exclude // tránh vòng lặp vô hạn khi in đối tượng User
    private Bank bank;

    // One-to-many relationship with Rooms rented by the tenant
    @ToString.Exclude
    @JsonIgnore // tránh vòng lặp vô hạn khi serializing đối tượng User
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Room> rooms = new HashSet<>();

    // One-to-one relationship with Landlord profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // khi xóa user thì xóa luôn landlordProfile
    @ToString.Exclude
    private Landlord landlordProfile;

    // Favorite landlord selected by the user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_landlord_id", nullable = true)
    @ToString.Exclude
    private Landlord favoriteLandlord;

    // Bookings made by the user
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();

    // Xu ảo của người dùng (tiền ảo trong hệ thống), 1x = 1vnd;
    @Column(nullable = false)
    @Builder.Default
    @NotNull(message = "Coins cannot be null")
    @Min(value = 0, message = "Coins must be non-negative")
    private Integer coins = 0;

    // Coupon history records associated with the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private Set<CouponHistory> couponHistories = new HashSet<>();

    // Mails associated with the user
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_mails", // bảng liên kết giữa User và Mail
                joinColumns = @JoinColumn(name = "user_id"), // khóa ngoại tham chiếu đến User
                inverseJoinColumns = @JoinColumn(name = "mail_id")) // khóa ngoại tham chiếu đến Mail
    @ToString.Exclude
    private Set<Mail> mails = new HashSet<>();

    // Payment history records associated with the user
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PaymentHistory> paymentHistories = new HashSet<>();

    // Payments made by the user
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Payment> payments = new HashSet<>();

    // Appreciations (favorites) made by the user
    @ToString.Exclude // tránh vòng lặp vô hạn khi in đối tượng User
    @JsonIgnore // tránh vòng lặp vô hạn khi serializing đối tượng User
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Appreciate> appreciates = new HashSet<>();

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
