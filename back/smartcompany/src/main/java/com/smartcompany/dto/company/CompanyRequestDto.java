package com.smartcompany.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompanyRequestDto(
    @NotBlank String name,
    String address,
    @NotNull Long countryId,
    @NotNull Long cityId,
    @NotNull Double latitude,
    @NotNull Double longitude,
    List<String> imageKeys,
    List<DocumentDto> documents
) {
    public record DocumentDto(
        @NotBlank String fileKey,
        @NotBlank String fileType // "image" ili "pdf"
    ) {}
}