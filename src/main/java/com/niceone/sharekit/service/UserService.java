package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.user.Role;
import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.dto.UserProfileRequest;
import com.niceone.sharekit.repository.RoleRepository;
import com.niceone.sharekit.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.Set;

/// 사용자 중복 방지 및 정보 조회/저장
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    // 생성자 수정
    public UserService(UserRepository userRepository,
                       @Lazy PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void registerNewUser(User user) {
        // TODO: 여기에 "ROLE_PRE_AUTH_USER" 역할 할당 로직 추가 예정
        userRepository.save(user);
    }

    @Transactional
    public void updateUserProfile(String uid, UserProfileRequest request) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> {
                    log.error("User not found with UID: {}", uid);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with UID: " + uid);
                });

        user.setName(request.getName());
        user.setStudentId(request.getStudentId());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        // TODO: 여기에 "ROLE_USER" 역할 할당 및 "ROLE_PRE_AUTH_USER" 역할 제거 로직 추가 예정
        userRepository.save(user);
        log.info("User UID: {} profile updated. Role update logic needs to be revised for Set<Role>.", uid);

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