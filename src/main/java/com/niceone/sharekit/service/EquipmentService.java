package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.equipment.Equipment;
import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import com.niceone.sharekit.dto.equipment.*;
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

        return EquipmentResponseDto.fromEntity(equipmentRepository.save(equipment));
    }

    // 특정 장비 정보 수정 (관리자 기능)
    @Transactional
    public EquipmentResponseDto updateEquipment(Long equipmentId, EquipmentUpdateRequestDto requestDto) {
        Equipment equipment = findEquipmentById(equipmentId);
        equipment.update(requestDto); // 엔티티의 update 메소드 호출
        return EquipmentResponseDto.fromEntity(equipment);
    }

    // RentalService에서 '반납 완료' 처리가 끝났을 때 호출
    @Transactional
    public void changeStatusToAvailable(Long equipmentId) {
        findEquipmentById(equipmentId).markAsAvailable();
    }

    // RentalService에서 '대여' 처리가 성공했을 때 호출
    @Transactional
    public void changeStatusToRented(Long equipmentId) {
        Equipment equipment = findEquipmentById(equipmentId);
        if (!equipment.isAvailable()) {
            throw new IllegalStateException("현재 대여 가능 상태가 아닌 장비입니다.");
        }
        equipment.markAsRented();
    }

    // 특정 장비 조회
    public EquipmentResponseDto getEquipmentById(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .map(EquipmentResponseDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("장비를 찾을 수 없습니다. ID: " + equipmentId));
    }

    // 전체 장비 리스트 조회
    public List<EquipmentResponseDto> findAll() {
        return equipmentRepository.findAll().stream()
                .map(EquipmentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 요약 정보 조회 메소드
    public List<EquipmentTypeSummaryDto> getEquipmentTypeSummaries() {
        Map<String, List<Equipment>> groupedByType = equipmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(Equipment::getTypeName));

        return groupedByType.entrySet().stream()
                .map(entry -> {
                    String typeName = entry.getKey();
                    List<Equipment> items = entry.getValue();

                    long totalCount = items.size(); // 총 개수 계산
                    long availableCount = items.stream() // 대여 가능 장비 수 계산
                            .filter(Equipment::isAvailable)
                            .count();

                    String imageUrl = items.stream() // 대표 이미지 추출
                            .map(Equipment::getImageUrl)
                            .filter(url -> url != null && !url.isBlank())
                            .findFirst()
                            .orElse(null);

                    return new EquipmentTypeSummaryDto(typeName, imageUrl, totalCount, availableCount);
                })
                .sorted(Comparator.comparing(EquipmentTypeSummaryDto::getTypeName))
                .collect(Collectors.toList());
    }

    // 장비 ID로 조회하는 헬퍼 메소드
    private Equipment findEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("장비를 찾을 수 없습니다. ID: " + id));
    }

    // 장비 타입에 따른 식별자 prefix 결정
    private String determinePrefix(String typeName) {
        return switch (typeName) {
            case "노트북" -> "LAPTOP";
            case "계산기" -> "CALC";
            case "충전기" -> "CHARGER";
            default -> "MISC";
        };
    }
}


