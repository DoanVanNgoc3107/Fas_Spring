package com.example.fas.model.enums.user;

// Statuses available in the system
public enum UserStatus {
    ACTIVE,  // Hoạt động
    BANNED_PERMANENT, // trạng thái bị cấm vĩnh viễn
    BANNED_1_DAY, // trạng thái bị 1 ngày
    BANNED_3_DAYS, // trạng thái bị cấm 3 ngày
    BANNED_7_DAYS, // cấm 7 ngày
    BANNED_30_DAYS, // Cấm 30 ngày
    DELETED
}