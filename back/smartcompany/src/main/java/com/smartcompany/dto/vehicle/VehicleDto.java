package com.smartcompany.dto.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record VehicleDto(
    Long id,
    String licensePlate,
    Double weightLimit,
    VehicleBrandDto brand,
    VehicleModelDto model,
    Double latitude,
    Double longitude,
    @JsonProperty("isOnline") boolean isOnline,
    String lastHeartbeat,
    List<String> imageKeys
) {}