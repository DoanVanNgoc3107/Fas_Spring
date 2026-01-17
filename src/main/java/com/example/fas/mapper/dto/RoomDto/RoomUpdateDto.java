package com.example.fas.mapper.dto.RoomDto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class RoomUpdateDto {
    @NotBlank(message = "Room name cannot be blank")
    private String roomName;

    @NotBlank(message = "Room description cannot be blank")
    private String description;

    @NotNull(message = "Max occupancy cannot be null")
    @Min(value = 1, message = "Max occupancy must be greater than 0")
    private int maxOccupancy;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotNull(message = "Image URLs cannot be null")
    private Set<String> imageUrls;

    @NotNull(message = "Area cannot be null")
    @Min(value = 1, message = "Area must be greater than 0 (m^2)")
    private int area;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0", inclusive = true, message = "Price must be greater than 0")
    @Digits(integer = 12, fraction = 0, message = "Price must be a valid monetary amount")
    private BigDecimal price;

    @NotNull(message = "Room status cannot be null")
    private String roomStatus;
}
