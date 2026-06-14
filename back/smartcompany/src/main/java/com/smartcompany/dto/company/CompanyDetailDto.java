package com.smartcompany.dto.company;

import com.smartcompany.dto.CityDto;
import com.smartcompany.dto.CountryDto;
import java.util.List;

public record CompanyDetailDto(
    Long id,
    String name,
    String address,
    CountryDto country,
    CityDto city,
    Double latitude,
    Double longitude,
    String status,
    String rejectionReason,
    String ownerUsername,
    List<String> imageKeys,
    List<DocumentDto> documents
) {
    public record DocumentDto(String fileKey, String fileType) {}
}