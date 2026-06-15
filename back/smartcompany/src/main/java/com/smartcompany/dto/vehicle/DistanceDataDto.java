package com.smartcompany.dto.vehicle;

import java.util.List;

public record DistanceDataDto(
    String granularity,
    List<String> labels,
    List<Double> values
) {}