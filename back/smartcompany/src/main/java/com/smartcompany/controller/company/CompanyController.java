package com.smartcompany.controller.company;

import com.smartcompany.dto.CityDto;
import com.smartcompany.dto.CountryDto;
import com.smartcompany.dto.UserDto;
import com.smartcompany.dto.company.CompanyDetailDto;
import com.smartcompany.dto.company.CompanyRequestDto;
import com.smartcompany.service.FactoryService;
import com.smartcompany.service.company.CompanyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
@Tag(name = "Companies", description = "Company registration requests")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final FactoryService factoryService;

    @PostMapping
    @PreAuthorize("hasRole('Customer')")
    public ResponseEntity<CompanyDetailDto> createRequest(
            @AuthenticationPrincipal UserDto user,
            @RequestBody @Valid CompanyRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(companyService.createRequest(user.getUsername(), dto));
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryDto>> getCountries() {
        return ResponseEntity.ok(factoryService.getAllCountries());
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityDto>> getCities(@RequestParam Long countryId) {
        return ResponseEntity.ok(factoryService.getCitiesByCountry(countryId));
    }
}