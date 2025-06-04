package com.niceone.sharekit.controller;

import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.dto.UserProfileRequest;
import com.niceone.sharekit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /// signup 에서 인증 받은 사용자의 프로필 업데이트
    @PutMapping("/me/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody UserProfileRequest profileRequest) {

        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증된 사용자 정보를 찾을 수 없습니다.");
        }

        String uid = authenticatedUser.getUid();

        try {
            userService.updateUserProfile(uid, profileRequest);

            log.info("User profile updated successfully for user UID: {}", uid);
            return ResponseEntity.ok("프로필 정보가 성공적으로 업데이트되었습니다.");

        } catch (Exception e) {
            log.error("Failed to update profile for user UID: {}", uid, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 업데이트 중 오류가 발생했습니다.", e);
        }
    }
}