package com.niceone.sharekit.dto.admin;

import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import com.niceone.sharekit.domain.rental.Rental;
import lombok.Getter;

@Getter
public class AdminRentalResponseDto {

    private final Long rentalId;
    private final String studentId;
    private final String userName;
    private final String equipmentName;
    private final String itemIdentifier;
    private final EquipmentStatus equipmentStatus;

    private AdminRentalResponseDto(Rental rental) {
        this.rentalId = rental.getId();
        this.studentId = rental.getUser().getStudentId();
        this.userName = rental.getUser().getName();
        this.equipmentName = rental.getEquipment().getName();
        this.itemIdentifier = rental.getEquipment().getItemIdentifier();
        this.equipmentStatus = rental.getEquipment().getStatus();
    }

    public static AdminRentalResponseDto fromEntity(Rental rental) {
        return new AdminRentalResponseDto(rental);
    }
}