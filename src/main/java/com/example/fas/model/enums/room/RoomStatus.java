package com.example.fas.model.enums.room;

// Room statuses available in the system
public enum RoomStatus {
    AVAILABLE, // Phòng có sẵn
    OCCUPIED, // Phòng đang được sử dụng
    RESERVED, // Phòng đã được đặt trước
    CLEANING, // Phòng đang được dọn dẹp
    MAINTENANCE, // Phòng đang bảo trì
    OUT_OF_SERVICE; // Phòng không hoạt động

    // Method to check if the room is available
    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    // Method to check if the room is occupied
    public boolean isOccupied() {
        return this == OCCUPIED;
    }

    // Method to check if the room is reserved
    public boolean isReserved() {
        return this == RESERVED;
    }

    // Method to check if the room is being cleaned
    public boolean isCleaning() {
        return this == CLEANING;
    }

    // Method to check if the room is under maintenance
    public boolean isUnderMaintenance() {
        return this == MAINTENANCE;
    }

    // Method to check if the room is out of service
    public boolean isOutOfService() {
        return this == OUT_OF_SERVICE;
    }
}
