package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.User;
import com.niceone.sharekit.dto.UserProfileRequest;
import com.niceone.sharekit.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Optional;

/// 사용자 중복 방지 및 정보 조회/저장
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerNewUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateUserProfile(String uid, UserProfileRequest request) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with UID: " + uid);
                });

        user.setName(request.getName());
        user.setStudentId(request.getStudentId());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setRole("ROLE_USER");
        userRepository.save(user);
        log.info("User UID: {} role updated to ROLE_USER after profile update.", uid);

    }

    @Transactional(readOnly = true)
    public Optional<User> findByUid(String uid) {
        return userRepository.findById(uid);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }
}