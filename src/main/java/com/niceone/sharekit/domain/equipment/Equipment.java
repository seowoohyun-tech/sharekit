package com.niceone.sharekit.domain.equipment;

import com.niceone.sharekit.domain.rental.Rental;
import com.niceone.sharekit.dto.equipment.EquipmentUpdateRequestDto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String itemIdentifier; // 개별 장비 식별자

    @Column(nullable = false, length = 150)
    private String name; // 장비의 구체적인 이름 또는 모델명

    @Column(nullable = false, length = 100)
    private String typeName; // 장비의 종류/분류

    @Column(length = 255)
    private String imageUrl; // 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EquipmentStatus status; // 장비 상태

    @Column(length = 200)
    private String rentalLocation; // 대여 가능 장소

    @Column(length = 255) // << "대여 가능 시간 정보" 필드 추가!
    private String availableTimeInfo; // 예: "평일 09:00-18:00", "상시 가능"

    @Column(length = 1000)
    private String description; // 장비에 대한 상세 설명

    @Column(length = 500)
    private String additionalInfo; // 기타 추가 정보

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Rental> rentals = new ArrayList<>();

    public void addRental(Rental rental) {
        this.rentals.add(rental);
        if (rental.getEquipment() != this) {
            rental.setEquipment(this);
        }
    }

    public boolean isAvailable() {
        return this.status == EquipmentStatus.AVAILABLE;
    }

    public void markAsRented() {
        this.status = EquipmentStatus.RENTED;
    }

    public void markAsAvailable() {
        this.status = EquipmentStatus.AVAILABLE;
    }

    public void markAsInRepair() {
        if (this.status == EquipmentStatus.RENTED) {
            throw new IllegalStateException("대여 중인 장비는 수리 상태로 변경할 수 없습니다.");
        }
        this.status = EquipmentStatus.IN_REPAIR;
    }

    //DTO로부터 받은 정보로 장비의 상세 내용 업데이트 
    public void update(EquipmentUpdateRequestDto requestDto) {
        if (requestDto.getName() != null) {
            this.name = requestDto.getName();
        }
        if (requestDto.getTypeName() != null) {
            this.typeName = requestDto.getTypeName();
        }
        if (requestDto.getImageUrl() != null) {
            this.imageUrl = requestDto.getImageUrl();
        }
        if (requestDto.getStatus() != null) {
            this.status = requestDto.getStatus();
        }
        if (requestDto.getRentalLocation() != null) {
            this.rentalLocation = requestDto.getRentalLocation();
        }
        if (requestDto.getAvailableTimeInfo() != null) {
            this.availableTimeInfo = requestDto.getAvailableTimeInfo();
        }
        if (requestDto.getDescription() != null) {
            this.description = requestDto.getDescription();
        }
        if (requestDto.getAdditionalInfo() != null) {
            this.additionalInfo = requestDto.getAdditionalInfo();
        }

    }
}
