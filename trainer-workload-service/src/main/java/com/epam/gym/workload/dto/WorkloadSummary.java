package com.epam.gym.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadSummary {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean trainerStatus;
    private List<YearSummary> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        private Integer year;
        private List<MonthSummary> months;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        private Integer month;
        private Integer trainingSummaryDuration;
    }
}
