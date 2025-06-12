package com.niceone.sharekit.controller;

import com.niceone.sharekit.dto.equipment.*;
import com.niceone.sharekit.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;


    /**
     * 메인 페이지: 장비 종류별 요약 정보 조회
     */
    @GetMapping("/equipments/summary")
    public ResponseEntity<List<EquipmentTypeSummaryDto>> getEquipmentSummaries() {
        return ResponseEntity.ok(equipmentService.getEquipmentTypeSummaries());
    }

    /**
     * 장비 목록 페이지: 특정 종류의 장비 목록 조회
     * @param typeName 조회할 장비의 종류
     */
    @GetMapping("/equipments/type/{typeName}")
    public ResponseEntity<List<EquipmentBriefDto>> getEquipmentsByType(@PathVariable String typeName) {
        return ResponseEntity.ok(equipmentService.getEquipmentListByType(typeName));
    }

    /**
     * 장비 상세 페이지: 특정 장비의 상세 정보 조회
     * @param equipmentId 조회할 장비의 ID
     */
    @GetMapping("/equipments/{equipmentId}")
    public ResponseEntity<EquipmentDetailDto> getEquipmentDetail(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(equipmentService.getEquipmentDetail(equipmentId));
    }


    // =================================================================
    // == 관리자용 API (ADMIN 권한 필요)
    // =================================================================

    /**
     * 관리자: 신규 장비 등록
     */
    @PostMapping("/admin/equipments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponseDto> createEquipment(@RequestBody EquipmentCreateRequestDto requestDto) {
        return ResponseEntity.ok(equipmentService.createEquipment(requestDto));
    }

    /**
     * 관리자: 장비 정보 수정
     */
    @PutMapping("/admin/equipments/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponseDto> updateEquipment(@PathVariable Long equipmentId, @RequestBody EquipmentUpdateRequestDto requestDto) {
        return ResponseEntity.ok(equipmentService.updateEquipment(equipmentId, requestDto));
    }

    /**
     * 관리자: 장비 삭제
     */
    @DeleteMapping("/admin/equipments/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long equipmentId) {
        equipmentService.deleteEquipment(equipmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 관리자: 장비 상태를 '수리 중'으로 변경
     */
    @PutMapping("/admin/equipments/{equipmentId}/repair")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsInRepair(@PathVariable Long equipmentId) {
        equipmentService.markEquipmentAsInRepair(equipmentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자: '수리 중'인 장비를 '대여 가능'으로 변경
     */
    @PutMapping("/admin/equipments/{equipmentId}/repair/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsAvailableAfterMaintenance(@PathVariable Long equipmentId) {
        equipmentService.markEquipmentAsAvailableAfterMaintenance(equipmentId);
        return ResponseEntity.ok().build();
    }
}