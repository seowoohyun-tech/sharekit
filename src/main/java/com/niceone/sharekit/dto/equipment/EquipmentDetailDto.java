package com.niceone.sharekit.dto.equipment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EquipmentDetailDto {
    private final Long id;
    private final String name;
    private final String rentalLocation;
    private final String displayStatus;
    private final LocalDate dueDate; // 반납일 (조건부 표시)
    private final List<StatusHistoryDto> statusHistories; // 상태 내역

    public EquipmentDetailDto(Long id, String name, String rentalLocation, String displayStatus, LocalDate dueDate, List<StatusHistoryDto> statusHistories) {
        this.id = id;
        this.name = name;
        this.rentalLocation = rentalLocation;
        this.displayStatus = displayStatus;
        this.dueDate = dueDate;
        this.statusHistories = statusHistories;
    }

    // 상태 내역을 위한 내부 DTO
    @Getter
    public static class StatusHistoryDto {
        private final String status;
        private final LocalDateTime changeDate;
        private final String note;

        public StatusHistoryDto(String status, LocalDateTime changeDate, String note) {
            this.status = status;
            this.changeDate = changeDate;
            this.note = note;
        }
    }
}