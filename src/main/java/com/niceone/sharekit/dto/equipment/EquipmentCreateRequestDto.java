package com.niceone.sharekit.dto.equipment;

import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EquipmentCreateRequestDto {

    @NotBlank(message = "장비 이름은 필수입니다.")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "장비 분류명은 필수입니다.")
    @Size(max = 100)
    private String typeName;

    @Size(max = 255)
    private String imageUrl;

    @NotNull(message = "장비 상태는 필수입니다.")
    private EquipmentStatus status;

    @NotBlank(message = "대여 가능 장소는 필수입니다.")
    @Size(max = 200)
    private String rentalLocation;

    @Size(max = 255)
    private String availableTimeInfo;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String additionalInfo;

    // ---- 공백 트리밍용 Setter Override ----
    public void setName(String name) {
        this.name = (name != null ? name.trim() : null);
    }

    public void setTypeName(String typeName) {
        this.typeName = (typeName != null ? typeName.trim() : null);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null ? imageUrl.trim() : null);
    }

    public void setRentalLocation(String rentalLocation) {
        this.rentalLocation = (rentalLocation != null ? rentalLocation.trim() : null);
    }

    public void setAvailableTimeInfo(String availableTimeInfo) {
        this.availableTimeInfo = (availableTimeInfo != null ? availableTimeInfo.trim() : null);
    }

    public void setDescription(String description) {
        this.description = (description != null ? description.trim() : null);
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = (additionalInfo != null ? additionalInfo.trim() : null);
    }
}
