package com.example.fas.model;

import com.example.fas.model.enums.withdraw.WithdrawStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

import java.math.BigDecimal;

@Entity
@Table(name = "withdraws")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Withdraw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên ngân hàng
    @NotBlank(message = "Bank name cannot be blank")
    @Pattern(regexp = "^[A-Za-z\\s]+$", message = "Bank name must contain only letters and spaces")
    private String bankName;

    // Số tài khoản ngân hàng
    @NotBlank(message = "Account number cannot be blank")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "Account number must be between 8 and 20 digits")
    private String accountNumber;

    // Tên chủ tài khoản ngân hàng
    @NotBlank(message = "Owner bank account cannot be blank")
    @Pattern(regexp = "^[A-Z\\s]+$", message = "Owner bank account must contain only uppercase letters and spaces")
    private String ownerBankAccount;

    // Nội dung rút tiền
    @NotBlank(message = "Bank content cannot be blank")
    @Column(updatable = false, columnDefinition = "TEXT")
    private String bankContent;

    // Số tiền rút (VND)
    @NotNull(message = "Amount cannot be null")
    @Column(nullable = false, precision = 19, scale = 0)
    @DecimalMin(value = "50000", inclusive = false, message = " Amount must be greater than or equal to 50,000 VND")
    @Digits(integer = 15, fraction = 0, message = "Amount must be a valid monetary amount (VND)")
    private BigDecimal amount;

    // Trạng thái rút tiền ( PENDING,REJECTED, APPROVED, REJECTED )
    @Builder.Default
    @NotNull(message = "Withdraw status cannot be null")
    @Enumerated(EnumType.STRING)
    private WithdrawStatus withdrawStatus = WithdrawStatus.PENDING;

    // Người dùng thực hiện rút tiền
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @NotNull(message = "Withdraw status cannot be null")
    private Instant createdAt;

    // Thời gian cập nhật lần cuối
    @JsonFormat(pattern = "dd/MM/yy/HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @NotNull(message = "Updated at cannot be null")
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
