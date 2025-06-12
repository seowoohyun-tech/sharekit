package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.equipment.Equipment;
import com.niceone.sharekit.domain.rental.Rental;
import com.niceone.sharekit.domain.rental.RentalStatus;
import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.dto.rental.AdminRentalUpdateRequestDto;
import com.niceone.sharekit.dto.rental.RentalRequestDto;
import com.niceone.sharekit.dto.rental.RentalResponseDto;
import com.niceone.sharekit.repository.EquipmentRepository;
import com.niceone.sharekit.repository.RentalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentService equipmentService;

    private static final int RENTAL_PERIOD_DAYS = 7;

    /**
     * 사용자: 장비 대여
     */
    public RentalResponseDto createRental(RentalRequestDto requestDto, User user) {
        Long equipmentId = requestDto.getEquipmentId();

        // 1. 비관적 잠금을 사용하여 동시 대여 방지
        Equipment equipment = equipmentRepository.findByIdWithPessimisticLock(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("장비를 찾을 수 없습니다. ID: " + equipmentId));

        if (!equipment.isAvailable()) {
            throw new IllegalStateException("현재 대여 가능한 상태가 아닌 장비입니다.");
        }

        // 2. 장비 상태 변경 및 이력 기록
        equipmentService.handleRental(equipment.getId(), user.getName());

        // 3. 대여 엔티티 생성 및 저장
        Rental rental = Rental.builder()
                .user(user)
                .equipment(equipment)
                .rentalDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(RENTAL_PERIOD_DAYS))
                .status(RentalStatus.RENTED)
                .build();
        
        user.addRental(rental);

        Rental savedRental = rentalRepository.save(rental);
        return new RentalResponseDto(savedRental);
    }

    /**
     * 관리자: 장비 반납 처리
     * @param rentalId 반납 처리할 대여 기록의 ID
     */
    public RentalResponseDto returnRentalByAdmin(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("대여 기록을 찾을 수 없습니다. ID: " + rentalId));
        
        if (rental.getStatus() != RentalStatus.RENTED) {
            throw new IllegalStateException("현재 '대여 중' 상태인 기록만 반납 처리할 수 있습니다.");
        }

        // 1. 장비 상태를 'AVAILABLE'로 변경하고 이력 기록
        equipmentService.handleReturn(rental.getEquipment().getId());

        // 2. 대여 기록의 상태를 'RETURNED'로 변경
        rental.complete();

        return new RentalResponseDto(rental);
    }
    
    /**
     * 관리자: '수리 보내기' 등 예외적인 상태 변경 처리
     * @param rentalId 변경할 대여 기록의 ID
     * @param updateDto 변경할 상태 정보를 담은 DTO
     */
    public RentalResponseDto updateRentalStatusByAdmin(Long rentalId, AdminRentalUpdateRequestDto updateDto) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("대여 기록을 찾을 수 없습니다. ID: " + rentalId));
        
        if (rental.getStatus() != RentalStatus.RENTED) {
            throw new IllegalStateException("현재 '대여 중' 상태인 기록만 상태를 변경할 수 있습니다.");
        }

        Long equipmentId = rental.getEquipment().getId();

        if ("IN_REPAIR".equals(updateDto.getNewStatus())) {
            // 1. 장비를 '수리 중' 상태로 변경하고 이력 기록
            equipmentService.markEquipmentAsInRepair(equipmentId);
            // 2. 대여 기록은 '반납' 처리하되, 사유를 남김
            rental.complete("관리자에 의해 수리 상태로 변경됨");
        } else {
            throw new IllegalArgumentException("유효하지 않은 상태 값입니다: " + updateDto.getNewStatus());
        }

        return new RentalResponseDto(rental);
    }
}