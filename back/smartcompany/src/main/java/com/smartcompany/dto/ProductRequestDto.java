package com.smartcompany.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequestDto(
    @NotBlank String name,
    String description,
    @NotNull @Positive BigDecimal price,
    @NotNull @Positive Double weight,
    String image,
    boolean isForSale,
    @NotNull Long categoryId,
    List<Long> factoryIds
) {}