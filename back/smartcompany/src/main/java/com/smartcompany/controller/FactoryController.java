package com.smartcompany.controller;

import com.smartcompany.dto.*;
import com.smartcompany.service.FactoryService;
import com.smartcompany.service.ProductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/factories")
@Tag(name = "Factories", description = "Factory management")
@SecurityRequirement(name = "bearerAuth")
public class FactoryController {

    @Autowired private FactoryService factoryService;
    @Autowired private ProductionService productionService;

    @Operation(summary = "Search factories")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<Page<FactoryDto>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Boolean isOnline,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(
            factoryService.search(search, countryId, cityId, isOnline, PageRequest.of(page, size, sort))
        );
    }

    @Operation(summary = "Get factory by ID")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<FactoryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(factoryService.getById(id));
    }

    @Operation(summary = "Create factory")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<FactoryDto> create(@RequestBody @Valid FactoryRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(factoryService.create(dto));
    }

    @Operation(summary = "Update factory")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<FactoryDto> update(@PathVariable Long id, @RequestBody @Valid FactoryRequestDto dto) {
        return ResponseEntity.ok(factoryService.update(id, dto));
    }

    @Operation(summary = "Delete factory")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        factoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all countries")
    @GetMapping(value = "/countries", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CountryDto>> getCountries() {
        return ResponseEntity.ok(factoryService.getAllCountries());
    }

    @Operation(summary = "Get cities by country")
    @GetMapping(value = "/cities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CityDto>> getCities(@RequestParam Long countryId) {
        return ResponseEntity.ok(factoryService.getCitiesByCountry(countryId));
    }

    @Operation(
        summary = "Get production data for chart (5.10)",
        description = "Period: WEEK | MONTH | THREE_MONTHS | SIX_MONTHS | YEAR | CUSTOM. " +
                      "For CUSTOM supply from and to as yyyy-MM-dd. Max 365 days."
    )
    @GetMapping(value = "/{factoryId}/products/{productId}/production",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<ProductionDataDto> getProductionData(
            @PathVariable Long factoryId,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "WEEK") String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ResponseEntity.ok(
            productionService.getProductionData(factoryId, productId, period, from, to)
        );
    }
}