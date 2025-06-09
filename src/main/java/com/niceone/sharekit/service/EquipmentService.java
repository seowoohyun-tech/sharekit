package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.equipment.Equipment;
import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import com.niceone.sharekit.dto.equipment.EquipmentCreateRequestDto;
import com.niceone.sharekit.dto.equipment.EquipmentResponseDto;
import com.niceone.sharekit.dto.equipment.EquipmentTypeSummaryDto; 
import com.niceone.sharekit.repository.EquipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator; 
import java.util.List;
import java.util.Map; 
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

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
        
        // 장비 이름 자동 넘버링 로직
        String newName = requestDto.getName() + " " + nextSequence + "번";

        // 동시성 이슈에 대비해 생성된 식별자가 이미 있는지 다시 한 번 확인
        equipmentRepository.findByItemIdentifier(newItemIdentifier).ifPresent(e -> {
            throw new IllegalStateException("동일한 장비 식별자가 이미 존재합니다. 다시 시도해주세요.");
        });

        Equipment equipment = Equipment.builder()
                .itemIdentifier(newItemIdentifier)
                .name(newName) // 자동 넘버링된 이름으로 설정
                .typeName(typeName)
                .imageUrl(requestDto.getImageUrl())
                .status(requestDto.getStatus()) 
                .rentalLocation(requestDto.getRentalLocation())
                .availableTimeInfo(requestDto.getAvailableTimeInfo())
                .description(requestDto.getDescription())
                .additionalInfo(requestDto.getAdditionalInfo())
                .build();
        
        Equipment savedEquipment = equipmentRepository.save(equipment);

        return EquipmentResponseDto.fromEntity(savedEquipment);
    }

    // RentalService에서 '반납 완료' 처리가 끝났을 때 이 메소드 호출
    // 장비 상태를 '대여 가능'으로 변경
    @Transactional
    public void changeStatusToAvailable(Long equipmentId) {
        findEquipmentById(equipmentId).markAsAvailable();
    }
    // RentalService에서 '대여' 처리가 성공했을 때 이 메소드 호출
    // 장비 상태를 '대여 중'으로 변경
    @Transactional
    public void changeStatusToRented(Long equipmentId) {
        Equipment equipment = findEquipmentById(equipmentId);
        if (!equipment.isAvailable()) {
            throw new IllegalStateException("현재 대여 가능 상태가 아닌 장비입니다.");
        }
        equipment.markAsRented();
    }

    public EquipmentResponseDto getEquipmentById(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .map(EquipmentResponseDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("장비를 찾을 수 없습니다. ID: " + equipmentId));
    }

    public List<EquipmentResponseDto> findAll() {
        return equipmentRepository.findAll().stream()
                .map(EquipmentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 요약 정보 조회 메소드
    public List<EquipmentTypeSummaryDto> getEquipmentTypeSummaries() {
        Map<String, List<Equipment>> groupedByType = equipmentRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(Equipment::getTypeName));

        return groupedByType.entrySet().stream()
                .map(entry -> {
                    String typeName = entry.getKey();
                    List<Equipment> items = entry.getValue();

                    long totalCount = items.size();
                    long availableCount = items.stream()
                            .filter(equipment -> equipment.getStatus() == EquipmentStatus.AVAILABLE)
                            .count();
                    
                    String imageUrl = items.stream()
                            .map(Equipment::getImageUrl)
                            .filter(url -> url != null && !url.isBlank())
                            .findFirst()
                            .orElse(null);

                    return new EquipmentTypeSummaryDto(typeName, imageUrl, totalCount, availableCount);
                })
                .sorted(Comparator.comparing(EquipmentTypeSummaryDto::getTypeName))
                .collect(Collectors.toList());
    }

    private Equipment findEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("장비를 찾을 수 없습니다. ID: ".concat(String.valueOf(id))));
    }

    private String determinePrefix(String typeName) {
        return switch (typeName) {
            case "노트북" -> "LAPTOP";
            case "계산기" -> "CALC";
            case "충전기" -> "CHARGER";
            default -> "MISC";
        };
    }
}


