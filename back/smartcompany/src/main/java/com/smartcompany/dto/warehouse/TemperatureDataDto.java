package com.smartcompany.dto.warehouse;

import java.util.List;

public record TemperatureDataDto(
    String granularity,
    List<String> labels,
    List<Double> values
) {}