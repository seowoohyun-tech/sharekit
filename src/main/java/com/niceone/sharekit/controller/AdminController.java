package com.niceone.sharekit.controller;

import com.niceone.sharekit.dto.admin.AdminRentalResponseDto;
import com.niceone.sharekit.dto.rental.RentalResponseDto;
import com.niceone.sharekit.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RentalService rentalService;

    /**
     * 관리자: 현재 '대여 중'인 모든 대여 기록 조회
     * @return 현재 대여 중인 장비 목록 (AdminRentalResponseDto 리스트)
     */
    @GetMapping("/rentals/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminRentalResponseDto>> getActiveRentals() {
        List<AdminRentalResponseDto> activeRentals = rentalService.getActiveRentals();
        return ResponseEntity.ok(activeRentals);
    }

    /**
     * 관리자: 장비 반납 처리
     * @param rentalId 반납 처리할 대여 기록의 ID
     */
    @PostMapping("/rentals/{rentalId}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalResponseDto> returnRentalByAdmin(@PathVariable Long rentalId) {
        RentalResponseDto responseDto = rentalService.returnRentalByAdmin(rentalId);
        return ResponseEntity.ok(responseDto);
    }
}