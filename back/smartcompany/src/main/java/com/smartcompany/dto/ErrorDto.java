package com.smartcompany.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standardna struktura greške koju vraća RestExceptionHandler")
public record ErrorDto(
    @Schema(description = "Poruka greške", example = "Product not found") String message
) {}