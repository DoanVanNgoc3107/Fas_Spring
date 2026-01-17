package com.example.fas.mapper.dto.RoomDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class RoomResponseDto {
    private long id;

    // Tên phòng
    private String roomName;

    // Mô tả phòng
    private String description;

    // Mã phòng
    private String roomCode;

    // Địa chỉ phòng
    private String address;

    // Danh sách URL hình ảnh của phòng
    private Set<String> imageUrls;

    // Số người tối đa có thể ở trong phòng
    private int maxOccupancy;

    // Diện tích phòng (m^2)
    private int area;

    // Giá phòng (vnđ)
    private BigDecimal price;

    // Trạng thái phòng
    private String roomStatus;

    // Tên của chủ nhà
    private long landlordName;

    // Danh sách đánh giá tích cực về phòng
    private Set<String> appreciates;

    // Tên của người thuê (nếu có)
    private String tenantName;
}
