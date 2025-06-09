package com.niceone.sharekit.dto.rental;

import com.niceone.sharekit.domain.rental.Rental;
import com.niceone.sharekit.domain.rental.RentalStatus;
import com.niceone.sharekit.dto.equipment.EquipmentResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@NoArgsConstructor
public class RentalResponseDto {

    private Long rentalId;
    private EquipmentResponseDto equipment; // 대여한 장비의 상세 정보
    private RentalStatus status;
    private LocalDateTime rentalDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private long overdueDays; // 연체 일수

    public RentalResponseDto(Rental rental) {
        this.rentalId = rental.getId();
        this.equipment = EquipmentResponseDto.fromEntity(rental.getEquipment());
        this.status = rental.getStatus();
        this.rentalDate = rental.getRentalDate();
        this.dueDate = rental.getDueDate();
        this.returnDate = rental.getReturnDate();
        this.overdueDays = calculateOverdueDays(rental);
    }

    public static RentalResponseDto fromEntity(Rental rental) {
        return new RentalResponseDto(rental);
    }

    private static long calculateOverdueDays(Rental rental) {
        if (rental.getDueDate() == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = rental.getDueDate();
        LocalDateTime returnDate = rental.getReturnDate();

        if (returnDate != null && returnDate.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate.toLocalDate(), returnDate.toLocalDate());
        } else if (returnDate == null && now.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate.toLocalDate(), now.toLocalDate());
        }

        return 0;
    }
}
