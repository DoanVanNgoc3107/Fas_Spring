package com.example.fas.mapper.dto.RoomDto;


import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class RoomRequestDto {

    // Tên phòng
    @NotBlank(message = "Room name cannot be blank")
    private String roomName;

    // Mô tả phòng
    @NotBlank(message = "Room description cannot be blank")
    private String description;

    // Số người tối đa có thể ở trong phòng
    @NotNull(message = "Max occupancy cannot be null")
    private int maxOccupancy;

    // Địa chỉ phòng
    @NotBlank(message = "Address cannot be blank")
    private String address;

    // Danh sách URL hình ảnh của phòng
    @NotNull(message = "Image URLs cannot be null")
    private Set<String> imageUrls;

    // Diện tích phòng (m^2)
    @NotNull(message = "Area cannot be null")
    private int area;

    // Giá phòng (vnđ)
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0", inclusive = true, message = "Price must be greater than 0")
    @Digits(integer = 12, fraction = 0, message = "Price must be a valid monetary amount")
    private BigDecimal price;

    // ID chủ nhà
    @NotNull(message = "Landlord ID cannot be null")
    @Min(value = 1, message = "Landlord ID must be greater than 0")
    private long landlordId;
}
