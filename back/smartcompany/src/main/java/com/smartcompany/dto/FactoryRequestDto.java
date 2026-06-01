package com.smartcompany.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FactoryRequestDto(
        @NotBlank String name,
        String address,
        @NotNull Long countryId,
        @NotNull Long cityId,
        @NotNull Double latitude,
        @NotNull Double longitude,
        List<String> imageKeys,   // S3 keys uploadovanih slika
        List<Long> productIds     // IDs proizvoda koje fabrika proizvodi
) {}