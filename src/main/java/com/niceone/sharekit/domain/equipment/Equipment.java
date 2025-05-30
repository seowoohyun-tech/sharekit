package com.niceone.sharekit.domain.equipment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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
    private String itemIdentifier;

    @Column(nullable = false, length = 100)
    private String typeName; 

    @Column(length = 255)
    private String imageUrl; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EquipmentStatus status; 

    @Column(length = 500)
    private String additionalInfo; 


    // Rental 엔티티와의 1:N 관계: 하나의 장비는 여러 대여 기록을 가질 수 있음
    // 'mappedBy = "equipment"'는 Rental 엔티티 내에 있는 'equipment' 필드에 의해 관계가 관리됨.
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Rental> rentals = new ArrayList<>();

    public void addRental(Rental rental) {
    this.rentals.add(rental); // 현재 Equipment의 rentals 목록에 rental 추가
    if (rental.getEquipment() != this) { // 무한루프 방지 및 중복 호출 방지 로직
        rental.setEquipment(this);
    }   
}
