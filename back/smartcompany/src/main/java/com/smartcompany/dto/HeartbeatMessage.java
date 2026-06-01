package com.smartcompany.dto;

import java.time.Instant;

public record HeartbeatMessage(
        Long factoryId,
        Instant timestamp
) {}