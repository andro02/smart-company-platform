package com.smartcompany.dto;

import java.time.Instant;

public record ProductionMessage(
        Long factoryId,
        Long productId,
        Integer quantity,
        Instant timestamp
) {}