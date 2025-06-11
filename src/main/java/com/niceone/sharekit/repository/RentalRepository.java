package com.niceone.sharekit.repository;

import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.domain.equipment.Equipment;
import com.niceone.sharekit.domain.rental.Rental;
import com.niceone.sharekit.domain.rental.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    // User 관련 조회
    List<Rental> findByUserOrderByRentalDateDesc(User user);
    List<Rental> findByUserUidOrderByRentalDateDesc(String uid);
    List<Rental> findByUserUidAndStatus(String uid, RentalStatus status);

    // Equipment 관련 조회
    Optional<Rental> findByEquipmentAndStatusIn(Equipment equipment, List<RentalStatus> statuses);

    // Rental 상태 관련 조회
    List<Rental> findByStatus(RentalStatus status);

     
     //현재 '대여 중(RENTED)' 상태인 대여 기록을 찾음
    Optional<Rental> findByEquipmentIdAndStatus(Long equipmentId, RentalStatus status);

    // 여러 장비에 대해 현재 '대여 중(RENTED)' 상태인 대여 기록들을 한 번의 쿼리로 조회
    List<Rental> findByEquipmentIdInAndStatus(List<Long> equipmentIds, RentalStatus status);
}