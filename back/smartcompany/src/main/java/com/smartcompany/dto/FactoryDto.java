package com.smartcompany.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;

import java.time.Instant;
import java.util.List;

public record FactoryDto(
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
        List<Long> productIds
) {}