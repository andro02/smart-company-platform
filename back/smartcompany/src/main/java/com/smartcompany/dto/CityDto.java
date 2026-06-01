package com.smartcompany.dto;

public record CityDto(
        Long id,
        String name,
        Long countryId
) {}