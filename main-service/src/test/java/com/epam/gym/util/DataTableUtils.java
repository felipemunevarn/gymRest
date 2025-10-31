package com.epam.gym.util;

import io.cucumber.datatable.DataTable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataTableUtils {
    private DataTableUtils() {}

    /**
     * Supports:
     * 1) Vertical 'key | value' table:
     *    | traineeUsername | antonia.neira  |
     *    | trainerUsername | maria.ramirez  |
     *    | type            | STRENGTH       |
     *    | date            | 2025-01-01     |
     *    | duration        | 60             |
     *    | description     | Upper body     |
     *
     * 2) Header row with one data row:
     *    | traineeUsername | trainerUsername | type     | date       | duration | description |
     *    | antonia.neira   | maria.ramirez   | STRENGTH | 2025-01-01 | 60       | Upper body  |
     */
    public static Map<String, String> toMap(DataTable table) {
        List<List<String>> rows = table.asLists(String.class);
        Map<String, String> map = new LinkedHashMap<>();

        if (rows.isEmpty()) return map;

        if (rows.size() == 1) {
            // Single row without header? Return empty to avoid surprises
            return map;
        }

        // Heuristic: if first row has 2 cols and there are multiple rows, assume key-value pairs
        if (rows.get(0).size() == 2 && rows.size() >= 1 && !rows.get(0).get(0).equalsIgnoreCase("key")) {
            for (List<String> r : rows) {
                if (r.size() < 2) continue;
                map.put(r.get(0).trim(), r.get(1).trim());
            }
            return map;
        }

        // Otherwise treat as header + one data row
        List<String> headers = rows.get(0);
        List<String> values  = rows.get(1);
        for (int i = 0; i < headers.size(); i++) {
            String key = headers.get(i) == null ? "" : headers.get(i).trim();
            String val = (i < values.size() && values.get(i) != null) ? values.get(i).trim() : "";
            map.put(key, val);
        }
        return map;
    }
}
