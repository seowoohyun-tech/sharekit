package com.niceone.sharekit.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.dto.LocalLoginRequest;
import com.niceone.sharekit.dto.SignupRequest;
import com.niceone.sharekit.dto.UserResponse;
import com.niceone.sharekit.service.FirebaseService;
import com.niceone.sharekit.service.JwtTokenProvider;
import com.niceone.sharekit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;



import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final FirebaseService firebaseService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /// 학번과 비밀번호로 로컬 로그인
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LocalLoginRequest loginRequest) {
        log.info("Attempting login with student ID: {}", loginRequest.getStudentId());

        Optional<User> userOptional = userService.findByStudentId(loginRequest.getStudentId());

        if (userOptional.isEmpty()) {
            log.warn("Login failed: User not found with student ID: {}", loginRequest.getStudentId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "학번 또는 비밀번호가 일치하지 않습니다."));
        }

        User user = userOptional.get();

        if (user.getPassword() == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Password mismatch for student ID: {}", loginRequest.getStudentId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "학번 또는 비밀번호가 일치하지 않습니다."));
        }

        // 비밀번호 일치 시 JWT 생성
        final String jwt = jwtTokenProvider.generateToken(user);

        log.info("User logged in successfully with student ID: {}", loginRequest.getStudentId());

        return ResponseEntity.ok(Map.of(
                "accessToken", jwt,
                "user", new UserResponse(user)
        ));
    }


    /// Firebase 기반 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest) {
        try {
            FirebaseToken decodedToken = firebaseService.verifyToken(signupRequest.getIdToken());
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            if (userService.findByUid(uid).isPresent()) {
                log.warn("Attempted signup for already existing user: {}", email);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 가입된 사용자입니다. 로그인해주세요.");
            }

            User newUser = User.builder()
                    .uid(uid)
                    .email(email)
                    .name(decodedToken.getName())
                    .build();

            userService.registerNewUser(newUser);

            log.info("New user signed up: {}", email);
            return ResponseEntity.ok("회원가입 완료. 프로필 정보를 입력해주세요.");

        } catch (FirebaseAuthException e) {
            log.error("Invalid Firebase Token during signup: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다.", e);
        } catch (Exception e) {
            log.error("Signup failed for token: {}", signupRequest.getIdToken(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 처리 중 오류가 발생했습니다.", e);
        }
    }
}