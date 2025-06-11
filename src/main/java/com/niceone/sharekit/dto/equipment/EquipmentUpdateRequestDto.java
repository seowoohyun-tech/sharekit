package com.niceone.sharekit.dto.equipment;

import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EquipmentUpdateRequestDto {

    // itemIdentifier는 보통 고유값이므로 수정 대상에서 제외합니다.

    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String typeName;

    @Size(max = 255)
    private String imageUrl;

    private EquipmentStatus status;

    @Size(max = 200)
    private String rentalLocation;

    @Size(max = 255)
    private String availableTimeInfo;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String additionalInfo;
}
