package com.smartcompany.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record WarehouseRequestDto(
    @NotBlank String name,
    String address,
    @NotNull Long countryId,
    @NotNull Long cityId,
    @NotNull Double latitude,
    @NotNull Double longitude,
    List<String> imageKeys,
    List<SectorRequestDto> sectors
) {
    public record SectorRequestDto(
        @NotBlank String name
    ) {}
}