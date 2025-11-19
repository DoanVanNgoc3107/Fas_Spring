package com.example.fas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contact")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="Contact name cannot be blank")
    @Column(nullable = false)
    private String emailContact;

    @NotBlank(message="Phone number cannot be blank")
    @Column(nullable = false)
    private String phoneNumberContact;

    @NotBlank(message="Address cannot be blank")
    @Column(nullable = false)
    private String addressContact;

    @NotBlank(message="Company name cannot be blank")
    @Column(nullable = false)
    private String companyName;
}
