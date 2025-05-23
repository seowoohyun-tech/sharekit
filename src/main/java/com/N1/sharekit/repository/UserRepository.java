package com.N1.sharekit.repository;

import com.N1.sharekit.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/// 사용자 정보를 위한 JPA Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByStudentId(String studentId);
}