package com.niceone.sharekit.controller;

import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.dto.rental.AdminRentalUpdateRequestDto;
import com.niceone.sharekit.dto.rental.RentalRequestDto;
import com.niceone.sharekit.dto.rental.RentalResponseDto;
import com.niceone.sharekit.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // =================================================================
    // == 사용자용 API (USER 권한 필요)
    // =================================================================

    /**
     * 사용자: 신규 장비 대여
     */
    @PostMapping("/rentals")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RentalResponseDto> createRental(
            @RequestBody RentalRequestDto requestDto,
            @AuthenticationPrincipal User user) { // Spring Security에서 현재 인증된 사용자 정보를 주입받습니다.
        RentalResponseDto responseDto = rentalService.createRental(requestDto, user);
        return ResponseEntity.ok(responseDto);
    }


    // =================================================================
    // == 관리자용 API (ADMIN 권한 필요)
    // =================================================================

    /**
     * 관리자: 장비 반납 처리
     * @param rentalId 반납 처리할 대여 기록의 ID
     */
    @PutMapping("/admin/rentals/{rentalId}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalResponseDto> returnRentalByAdmin(@PathVariable Long rentalId) {
        RentalResponseDto responseDto = rentalService.returnRentalByAdmin(rentalId);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 관리자: 대여 기록 상태 변경 (예: 수리 중)
     * @param rentalId 상태를 변경할 대여 기록의 ID
     */
    @PatchMapping("/admin/rentals/{rentalId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalResponseDto> updateRentalStatusByAdmin(
            @PathVariable Long rentalId,
            @RequestBody AdminRentalUpdateRequestDto requestDto) {
        RentalResponseDto responseDto = rentalService.updateRentalStatusByAdmin(rentalId, requestDto);
        return ResponseEntity.ok(responseDto);
    }
}