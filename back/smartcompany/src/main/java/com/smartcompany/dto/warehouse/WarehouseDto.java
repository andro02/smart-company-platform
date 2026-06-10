package com.smartcompany.dto.warehouse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartcompany.dto.CityDto;
import com.smartcompany.dto.CountryDto;

import java.util.List;

public record WarehouseDto(
    Long id,
    String name,
    String address,
    CountryDto country,
    CityDto city,
    Double latitude,
    Double longitude,
    @JsonProperty("isOnline") boolean isOnline,
    String lastHeartbeat,
    List<String> imageKeys,
    List<SectorDto> sectors
) {
    public record SectorDto(
        Long id,
        String name,
        Double lastTemperature,
        String lastMeasuredAt
    ) {}
}