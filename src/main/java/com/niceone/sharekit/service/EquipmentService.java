package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.equipment.Equipment;
import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import com.niceone.sharekit.domain.equipment.EquipmentStatusHistory;
import com.niceone.sharekit.domain.rental.Rental;
import com.niceone.sharekit.domain.rental.RentalStatus;
import com.niceone.sharekit.dto.equipment.*;
import com.niceone.sharekit.repository.EquipmentRepository;
import com.niceone.sharekit.repository.EquipmentStatusHistoryRepository;
import com.niceone.sharekit.repository.RentalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RentalRepository rentalRepository;
    private final EquipmentStatusHistoryRepository statusHistoryRepository;

    // 장비 생성 메소드
    @Transactional
    public EquipmentResponseDto createEquipment(EquipmentCreateRequestDto requestDto) {
        String typeName = requestDto.getTypeName();
        String prefix = determinePrefix(typeName);
        String fullPrefix = prefix + "-";

        int nextSequence = equipmentRepository.findTopByItemIdentifierStartsWithOrderByItemIdentifierDesc(fullPrefix)
                .map(lastEquipment -> {
                    String lastIdentifier = lastEquipment.getItemIdentifier();
                    String numericPart = lastIdentifier.substring(fullPrefix.length());
                    return Integer.parseInt(numericPart) + 1;
                })
                .orElse(1);

        String newItemIdentifier = String.format("%s%03d", fullPrefix, nextSequence);
        String newName = requestDto.getName() + " " + nextSequence + "번"; // 장비 이름 자동 넘버링

        // 동시성 이슈에 대비해 생성된 식별자가 이미 있는지 다시 확인
        equipmentRepository.findByItemIdentifier(newItemIdentifier).ifPresent(e -> {
            throw new IllegalStateException("동일한 장비 식별자가 이미 존재합니다. 다시 시도해주세요.");
        });

        Equipment equipment = Equipment.builder()
                .itemIdentifier(newItemIdentifier)
                .name(newName)
                .typeName(typeName)
                .imageUrl(requestDto.getImageUrl())
                .status(requestDto.getStatus())
                .rentalLocation(requestDto.getRentalLocation())
                .availableTimeInfo(requestDto.getAvailableTimeInfo())
                .description(requestDto.getDescription())
                .additionalInfo(requestDto.getAdditionalInfo())
                .build();
        
        Equipment savedEquipment = equipmentRepository.save(equipment);
        
        recordStatusHistory(savedEquipment, savedEquipment.getStatus(), "장비 등록");

        return EquipmentResponseDto.fromEntity(savedEquipment);
    }

    // 특정 장비 정보 수정 (관리자 기능)
    @Transactional
    public EquipmentResponseDto updateEquipment(Long equipmentId, EquipmentUpdateRequestDto requestDto) {
        Equipment equipment = findEquipmentById(equipmentId);
        equipment.update(requestDto); // 엔티티의 update 메소드 호출
        return EquipmentResponseDto.fromEntity(equipment);
    }

    
    // 장비 타입별 요약 정보 조회
    @Transactional(readOnly = true)
    public List<EquipmentTypeSummaryDto> getEquipmentTypeSummaries() {
        Map<String, List<Equipment>> groupedByType = equipmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(Equipment::getTypeName));

        return groupedByType.entrySet().stream()
                .map(entry -> {
                    String typeName = entry.getKey();
                    List<Equipment> items = entry.getValue();
                    long totalCount = items.size();
                    long availableCount = items.stream().filter(Equipment::isAvailable).count();
                    String imageUrl = items.stream()
                            .map(Equipment::getImageUrl)
                            .filter(url -> url != null && !url.isBlank())
                            .findFirst().orElse(null);
                    return new EquipmentTypeSummaryDto(typeName, imageUrl, totalCount, availableCount);
                })
                .sorted(Comparator.comparing(EquipmentTypeSummaryDto::getTypeName))
                .collect(Collectors.toList());
    }

    // 특정 타입의 모든 장비 목록 조회
    @Transactional(readOnly = true)
    public List<EquipmentBriefDto> getEquipmentListByType(String typeName) {
        List<Equipment> equipmentList = equipmentRepository.findByTypeName(typeName);
        List<Long> equipmentIds = equipmentList.stream().map(Equipment::getId).collect(Collectors.toList());
        Map<Long, Rental> activeRentalMap = getActiveRentalMap(equipmentIds);

        return equipmentList.stream()
                .map(equipment -> {
                    Optional<Rental> rentalOpt = Optional.ofNullable(activeRentalMap.get(equipment.getId()));
                    String displayStatus = determineDisplayStatus(equipment, rentalOpt);
                    return new EquipmentBriefDto(equipment.getId(), equipment.getName(), equipment.getItemIdentifier(), displayStatus);
                })
                .collect(Collectors.toList());
    }

    
    // 장비 상세 정보 조회
    @Transactional(readOnly = true)
    public EquipmentDetailDto getEquipmentDetail(Long equipmentId) {
        Equipment equipment = findEquipmentById(equipmentId);
        Optional<Rental> rentalOpt = rentalRepository.findByEquipmentIdAndStatus(equipmentId, RentalStatus.RENTED);
        
        String displayStatus = determineDisplayStatus(equipment, rentalOpt);
        LocalDate dueDate = getDueDateIfRented(displayStatus, rentalOpt);
        
        List<EquipmentDetailDto.StatusHistoryDto> historyDtos = statusHistoryRepository.findByEquipmentIdOrderByChangeDateDesc(equipmentId)
                .stream()
                .map(history -> new EquipmentDetailDto.StatusHistoryDto(
                        mapStatusToKorean(history.getStatus()), 
                        history.getChangeDate(), 
                        history.getNote()))
                .collect(Collectors.toList());

        return new EquipmentDetailDto(equipment.getId(), equipment.getName(), equipment.getRentalLocation(), displayStatus, dueDate, historyDtos);
    }
    

    @Transactional
    public void markEquipmentAsInRepair(Long equipmentId) {
        Equipment equipment = findEquipmentById(equipmentId);
        equipment.markAsInRepair(); 
        recordStatusHistory(equipment, EquipmentStatus.IN_REPAIR, "수리 중");
    }

    @Transactional
    public void markEquipmentAsAvailableAfterMaintenance(Long equipmentId) {
        Equipment equipment = findEquipmentById(equipmentId);
        if (equipment.getStatus() != EquipmentStatus.IN_REPAIR) {
            throw new IllegalStateException("'수리 중' 상태인 장비만 '대여 가능'으로 변경할 수 있습니다.");
        }
        equipment.markAsAvailable();
        recordStatusHistory(equipment, EquipmentStatus.AVAILABLE, "수리 완료");
    }

    @Transactional
    public void handleReturn(Long equipmentId) {
        Equipment equipment = findEquipmentById(equipmentId);
        equipment.markAsAvailable();
        recordStatusHistory(equipment, EquipmentStatus.AVAILABLE, "반납 완료");
    }

    @Transactional
    public void handleRental(Long equipmentId, String userIdentifier) {
        Equipment equipment = findEquipmentById(equipmentId);
        equipment.markAsRented();
        recordStatusHistory(equipment, EquipmentStatus.RENTED, userIdentifier + " 대여");
    }

    @Transactional
    public void deleteEquipment(Long equipmentId) {
        Equipment equipment = findEquipmentById(equipmentId);
        if (equipment.getStatus() == EquipmentStatus.RENTED) {
            throw new IllegalStateException("대여 중인 장비는 삭제할 수 없습니다.");
        }
        equipmentRepository.deleteById(equipmentId);
    }


    public Equipment findEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("장비를 찾을 수 없습니다. ID: " + id));
    }

    private String determinePrefix(String typeName) {
        return switch (typeName) {
            case "노트북" -> "LAPTOP";
            case "계산기" -> "CALC";
            case "충전기" -> "CHARGER";
            default -> "MISC";
        };
    }

    private String determineDisplayStatus(Equipment equipment, Optional<Rental> activeRentalOpt) {
        return switch (equipment.getStatus()) {
            case AVAILABLE -> "대여 가능";
            case IN_REPAIR -> "수리 중";
            case RENTED -> activeRentalOpt.map(rental ->
                    rental.getDueDate().toLocalDate().isBefore(LocalDate.now()) ? "연체 중" : "대여 중"
            ).orElse("상태 오류");
            default -> "기타";
        };
    }

    private LocalDate getDueDateIfRented(String displayStatus, Optional<Rental> rentalOpt) {
        if ("대여 중".equals(displayStatus)) {
            return rentalOpt.map(rental -> rental.getDueDate().toLocalDate()).orElse(null);
        }
        return null;
    }

    private Map<Long, Rental> getActiveRentalMap(List<Long> equipmentIds) {
        return rentalRepository
                .findByEquipmentIdInAndStatus(equipmentIds, RentalStatus.RENTED)
                .stream()
                .collect(Collectors.toMap(r -> r.getEquipment().getId(), r -> r));
    }

    private void recordStatusHistory(Equipment equipment, EquipmentStatus status, String note) {
        statusHistoryRepository.save(
            EquipmentStatusHistory.builder()
                .equipment(equipment)
                .status(status)
                .note(note)
                .build()
        );
    }
    
    private String mapStatusToKorean(EquipmentStatus status) {
        return switch (status) {
            case AVAILABLE -> "대여 가능";
            case RENTED -> "대여 중";
            case IN_REPAIR -> "수리 중";
            default -> "기타";
        };
    }
}



