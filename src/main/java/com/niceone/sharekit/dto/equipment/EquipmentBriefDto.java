package com.niceone.sharekit.dto.equipment;

import lombok.Getter;

@Getter
public class EquipmentBriefDto {
    private final Long id;
    private final String name;
    private final String itemIdentifier;
    private final String displayStatus; // "대여 가능", "대여 중", "연체 중", "수리 중"

    public EquipmentBriefDto(Long id, String name, String itemIdentifier, String displayStatus) {
        this.id = id;
        this.name = name;
        this.itemIdentifier = itemIdentifier;
        this.displayStatus = displayStatus;
    }
}