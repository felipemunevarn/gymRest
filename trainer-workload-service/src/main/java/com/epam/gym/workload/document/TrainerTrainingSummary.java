package com.epam.gym.workload.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "trainer_training_summaries")
@CompoundIndex(def = "{'trainerFirstName': 1, 'trainerLastName': 1}", name = "trainer_name_index")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerTrainingSummary {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean trainerStatus; // Boolean type as required

    @Valid
    @Builder.Default
    private List<YearSummary> years = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        @NotNull
        private Integer year;

        @Valid
        @Builder.Default
        private List<MonthSummary> months = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        @NotNull
        private Integer month; // 1-12

        @NotNull
        @Builder.Default
        private Integer trainingSummaryDuration = 0; // Number type as required
    }
}