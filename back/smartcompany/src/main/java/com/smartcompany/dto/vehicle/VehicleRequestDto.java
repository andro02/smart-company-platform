package com.smartcompany.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record VehicleRequestDto(
    @NotBlank String licensePlate,
    @NotNull @Positive Double weightLimit,
    @NotNull Long brandId,
    @NotNull Long modelId,
    List<String> imageKeys
) {}