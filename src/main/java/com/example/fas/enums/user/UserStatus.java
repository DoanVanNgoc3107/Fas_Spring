package com.example.fas.enums.user;

// Statuses available in the system
public enum UserStatus {
    ACTIVE, 
    BANNED_PERMANENT, // trạng thái bị cấm vĩnh viễn
    BANNED_3_DAY, // trạng thái bị cấm tạm thời
    BANNED_7_DAY,
    BANNED_30_DAY,
    DELETED
}