package com.example.fas.model.enums.room;

// Room statuses available in the system
public enum RoomStatus {
    AVAILABLE, // Phòng có sẵn
    OCCUPIED, // Phòng đang được sử dụng
    RESERVED, // Phòng đã được đặt trước
    CLEANING, // Phòng đang được dọn dẹp
    MAINTENANCE, // Phòng đang bảo trì
    OUT_OF_SERVICE; // Phòng không hoạt động
}
