package com.niceone.sharekit.repository;

import com.niceone.sharekit.domain.equipment.Equipment;
import com.niceone.sharekit.domain.equipment.EquipmentStatus;
import jakarta.persistence.LockModeType; // LockModeType 임포트 추가됨 (좋음)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;     // @Lock 임포트 추가됨 (좋음)
import org.springframework.data.jpa.repository.Query;    // @Query 임포트 추가됨 (좋음)
import org.springframework.data.repository.query.Param;  // @Param 임포트 추가됨 (좋음)
import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // 개별 장비 식별자로 조회
    Optional<Equipment> findByItemIdentifier(String itemIdentifier);

    // 장비 분류명으로 조회
    List<Equipment> findByTypeName(String typeName);

    // 장비 상태로 조회
    List<Equipment> findByStatus(EquipmentStatus status);

    // 장비 분류명과 상태로 조회
    List<Equipment> findByTypeNameAndStatus(String typeName, EquipmentStatus status);

    // 장비 분류명(typeName)에 특정 문자열을 포함하는 장비 검색 (부분 문자열 검색)
    List<Equipment> findByTypeNameContaining(String keyword);

    // 동시성 제어를 위한 비관적 쓰기 잠금을 사용하는 조회 메소드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.itemIdentifier = :itemIdentifier")
    Optional<Equipment> findByItemIdentifierForUpdate(@Param("itemIdentifier") String itemIdentifier);

}