package com.smartcompany.controller.warehouse;

import com.smartcompany.dto.*;
import com.smartcompany.dto.warehouse.TemperatureDataDto;
import com.smartcompany.dto.warehouse.WarehouseDto;
import com.smartcompany.dto.warehouse.WarehouseRequestDto;
import com.smartcompany.service.warehouse.TemperatureService;
import com.smartcompany.service.warehouse.WarehouseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@Tag(name = "Warehouses", description = "Warehouse management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final com.smartcompany.service.FactoryService factoryService;
    private final TemperatureService temperatureService;

    @GetMapping
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<Page<WarehouseDto>> search(
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
            warehouseService.search(search, countryId, cityId, isOnline,
                PageRequest.of(page, size, sort))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<WarehouseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<WarehouseDto> create(@RequestBody @Valid WarehouseRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<WarehouseDto> update(@PathVariable Long id,
                                                @RequestBody @Valid WarehouseRequestDto dto) {
        return ResponseEntity.ok(warehouseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Reuse factory endpoints za countries i cities
    @GetMapping("/countries")
    public ResponseEntity<List<CountryDto>> getCountries() {
        return ResponseEntity.ok(factoryService.getAllCountries());
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityDto>> getCities(@RequestParam Long countryId) {
        return ResponseEntity.ok(factoryService.getCitiesByCountry(countryId));
    }

    @GetMapping("/{warehouseId}/sectors/{sectorId}/temperature")
    @PreAuthorize("hasAnyRole('Manager', 'SuperAdmin')")
    public ResponseEntity<TemperatureDataDto> getTemperatureData(
            @PathVariable Long warehouseId,
            @PathVariable Long sectorId,
            @RequestParam(defaultValue = "WEEK") String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return ResponseEntity.ok(
            temperatureService.getTemperatureData(warehouseId, sectorId, period, from, to)
        );
    }
}
