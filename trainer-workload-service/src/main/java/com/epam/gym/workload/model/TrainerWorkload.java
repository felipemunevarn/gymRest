package com.epam.gym.workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;

    // Map structure: Year -> Month -> Total Duration
    @Builder.Default
    private Map<Integer, Map<Integer, Integer>> yearlyWorkload = new HashMap<>();

    public void addTraining(int year, int month, int duration) {
        yearlyWorkload.computeIfAbsent(year, k -> new HashMap<>())
                .merge(month, duration, Integer::sum);
    }

    public void removeTraining(int year, int month, int duration) {
        Map<Integer, Integer> monthlyWorkload = yearlyWorkload.get(year);
        if (monthlyWorkload != null) {
            Integer currentDuration = monthlyWorkload.get(month);
            if (currentDuration != null) {
                int newDuration = Math.max(0, currentDuration - duration);
                if (newDuration == 0) {
                    monthlyWorkload.remove(month);
                    if (monthlyWorkload.isEmpty()) {
                        yearlyWorkload.remove(year);
                    }
                } else {
                    monthlyWorkload.put(month, newDuration);
                }
            }
        }
    }

    public int getMonthlyDuration(int year, int month) {
        return yearlyWorkload.getOrDefault(year, new HashMap<>())
                .getOrDefault(month, 0);
    }
}
