package com.example.fas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "locations")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "Province cannot be blank")
    private String province; // tỉnh/thành phố trực thuộc trung ương

    @NotBlank(message = "District cannot be blank")
    private String district; // quận/huyện/thị xã

    @NotBlank(message = "District cannot be blank")
    private String addressDetails; // địa chỉ cụ thể

    @OneToOne(mappedBy = "address")
    @JsonIgnore
    @ToString.Exclude
    private Room room;
}
