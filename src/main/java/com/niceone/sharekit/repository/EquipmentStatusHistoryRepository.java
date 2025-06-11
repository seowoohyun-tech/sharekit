package com.niceone.sharekit.repository;

import com.niceone.sharekit.domain.equipment.EquipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentStatusHistoryRepository extends JpaRepository<EquipmentStatusHistory, Long> {
    
    List<EquipmentStatusHistory> findByEquipmentIdOrderByChangeDateDesc(Long equipmentId);
}