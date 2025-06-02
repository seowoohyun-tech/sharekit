package com.niceone.sharekit.domain.rental;

import com.niceone.sharekit.domain.User; // User 엔티티의 실제 패키지 경로로 수정해주세요.
import com.niceone.sharekit.domain.equipment.Equipment; // 우리가 정의한 단일 Equipment 엔티티
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 대여 사용자 (User와 N:1 관계)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment; // 대여 장비 (Equipment 엔티티와 N:1 관계)

    @Column(nullable = false)
    private LocalDateTime rentalDate; // 대여일시

    @Column(nullable = false)
    private LocalDateTime dueDate; // 반납 예정일시

    private LocalDateTime returnDate; // 실제 반납일시 (nullable)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RentalStatus status; // 대여 상태 (Enum: RENTED, RETURNED, OVERDUE 등)

}