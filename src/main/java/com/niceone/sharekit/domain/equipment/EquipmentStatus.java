package com.niceone.sharekit.domain.equipment;

public enum EquipmentStatus {
    AVAILABLE,  // 대여 가능
    RENTED,     // 대여 중 
    IN_REPAIR,  // 수리 중
    UNAVAILABLE // 대여 불가(게타 사유유)
}
