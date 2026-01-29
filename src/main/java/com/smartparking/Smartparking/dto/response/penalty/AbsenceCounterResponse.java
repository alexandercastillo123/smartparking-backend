package com.smartparking.Smartparking.dto.response.penalty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceCounterResponse {
    private String userId;
    private Integer absenceCount;
    private Integer strikeCount;
    private Integer maxStrikes;
    private LocalDateTime lastUpdated;
    private String status; // "safe", "warning", "danger", "suspended"
    private String message;
}
