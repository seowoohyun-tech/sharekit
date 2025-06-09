package com.niceone.sharekit.dto.rental;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RentalCreateRequestDto {

    @NotBlank(message = "대여하려는 장비의 고유 식별자(itemIdentifier)는 필수입니다.")
    private String equipmentItemIdentifier;

}