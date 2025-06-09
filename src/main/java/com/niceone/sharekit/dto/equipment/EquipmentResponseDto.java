package com.niceone.sharekit.dto.equipment;

import com.niceone.sharekit.domain.equipment.Equipment;
import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EquipmentResponseDto {

    private Long id;
    private String itemIdentifier;
    private String name;
    private String typeName;
    private String imageUrl;
    private EquipmentStatus status;
    private String rentalLocation;
    private String availableTimeInfo;
    private String description;
    private String additionalInfo;

    public static EquipmentResponseDto fromEntity(Equipment equipment) {
        EquipmentResponseDto dto = new EquipmentResponseDto();
        dto.id = equipment.getId();
        dto.itemIdentifier = equipment.getItemIdentifier();
        dto.name = equipment.getName();
        dto.typeName = equipment.getTypeName();
        dto.imageUrl = equipment.getImageUrl();
        dto.status = equipment.getStatus();
        dto.rentalLocation = equipment.getRentalLocation();
        dto.availableTimeInfo = equipment.getAvailableTimeInfo();
        dto.description = equipment.getDescription();
        dto.additionalInfo = equipment.getAdditionalInfo();
        return dto;
    }
}
