package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.user.Role;
import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.dto.user.UserProfileRequest;
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
        Role preAuthUserRole = roleRepository.findByName("ROLE_PRE_AUTH_USER")
                .orElseThrow(() -> {
                    log.error("'ROLE_PRE_AUTH_USER' not found in database. Check DataInitializer.");
                    return new IllegalStateException("'ROLE_PRE_AUTH_USER' not found. This is a server configuration issue.");
                });
        user.getRoles().add(preAuthUserRole);
        userRepository.save(user);
        log.info("New user registered with UID: {} and granted ROLE_PRE_AUTH_USER", user.getUid());
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

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("'ROLE_USER' not found in database. Check DataInitializer.");
                    return new IllegalStateException("'ROLE_USER' not found. This is a server configuration issue.");
                });
        user.getRoles().add(userRole);
        log.info("Role 'ROLE_USER' added to user UID: {}", uid);

        Optional<Role> preAuthUserRoleOptional = roleRepository.findByName("ROLE_PRE_AUTH_USER");
        if (preAuthUserRoleOptional.isPresent()) {
            if (user.getRoles().contains(preAuthUserRoleOptional.get())) {
                user.getRoles().remove(preAuthUserRoleOptional.get());
                log.info("Role 'ROLE_PRE_AUTH_USER' removed from user UID: {}", uid);
            } else {
                log.info("User UID: {} did not have 'ROLE_PRE_AUTH_USER' to remove.", uid);
            }
        } else {
            log.warn("'ROLE_PRE_AUTH_USER' not found in database for removal. Check DataInitializer.");
        }

        userRepository.save(user);
        log.info("User UID: {} profile updated. profile updated successfully. Roles adjusted.", uid);

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