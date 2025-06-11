package com.niceone.sharekit.dto.equipment;

import lombok.Getter;

@Getter
public class EquipmentTypeSummaryDto {
    private String typeName; 
    private String imageUrl; 
    private long totalCount; 
    private long availableCount; // 현재 대여 가능한 장비 개수수

    public EquipmentTypeSummaryDto(String typeName, String imageUrl, long totalCount, long availableCount) {
        this.typeName = typeName;
        this.imageUrl = imageUrl;
        this.totalCount = totalCount;
        this.availableCount = availableCount;
    }
}