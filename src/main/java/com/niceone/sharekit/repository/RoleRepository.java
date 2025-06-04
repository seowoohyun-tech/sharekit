package com.niceone.sharekit.repository;

import com.niceone.sharekit.domain.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // 역할 이름(name)으로 Role 엔티티를 조회하는 메소드
    // 해당 이름의 역할이 없을 수도 있으므로 Optional<Role>을 반환합니다.
    Optional<Role> findByName(String name);
}