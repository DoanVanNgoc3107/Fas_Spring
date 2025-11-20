package com.example.fas.model;

import com.example.fas.model.enums.mail.MailStatus;
import com.example.fas.model.enums.mail.MailType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mails")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email title cannot be blank")
    private String emailTitle;

    @NotBlank(message = "Email author cannot be blank")
    private String emailAuthor;

    @NotBlank(message = "Email content cannot be blank")
    private String emailContent;

    @JsonIgnore
    @ToString.Exclude
    @Column(nullable = false)
    @ManyToMany(mappedBy = "mails")
    private Set<User> users = new HashSet<>();

    @JsonIgnore
    @Column(nullable = true)
    @ManyToMany(mappedBy = "mails")
    @ToString.Exclude
    private Set<Coupon> coupons = new HashSet<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    @NotNull(message = "Email status cannot be null")
    private MailStatus emailStatus = MailStatus.READ; // read, unread

    @Builder.Default
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Email type cannot be null")
    private MailType emailType = MailType.NOTIFICATION; // notification, promotion, alert

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC+7")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC+7")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.createdAt == null) this.updatedAt = now;

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
