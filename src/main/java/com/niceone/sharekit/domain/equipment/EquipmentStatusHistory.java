package com.niceone.sharekit.domain.equipment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 생성일 자동 기록
public class EquipmentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status; 

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime changeDate; // 상태 변경일

    private String note; // 비고 (ex: "사용자 A가 대여", "수리 완료")

    @Builder
    public EquipmentStatusHistory(Equipment equipment, EquipmentStatus status, String note) {
        this.equipment = equipment;
        this.status = status;
        this.note = note;
    }
}
