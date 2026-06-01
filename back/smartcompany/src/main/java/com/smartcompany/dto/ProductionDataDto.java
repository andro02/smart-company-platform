package com.smartcompany.dto;

import java.util.List;

public record ProductionDataDto(
        String granularity,   // "day" | "week" | "month"
        List<String> labels,  // x-osa labele
        List<Long> values     // y-osa vrednosti
) {}