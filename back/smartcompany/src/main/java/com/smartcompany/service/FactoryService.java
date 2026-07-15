package com.smartcompany.service;

import com.smartcompany.dto.*;
import com.smartcompany.exception.AppException;
import com.smartcompany.model.*;
import com.smartcompany.repository.*;
import com.smartcompany.specification.FactorySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class FactoryService {

    @Autowired private FactoryRepository factoryRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InfluxDBService influxDBService;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public Page<FactoryDto> search(String search, Long countryId, Long cityId,
                                   Boolean isOnline, Pageable pageable) {
        Specification<Factory> spec = FactorySpecification.search(search, countryId, cityId, isOnline);
        return factoryRepository.findAll(spec, pageable).map(this::toDto);
    }

    public FactoryDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public FactoryDto create(FactoryRequestDto dto) {
        Country country = countryRepository.findById(dto.countryId())
                .orElseThrow(() -> new AppException("Country not found", HttpStatus.NOT_FOUND));
        City city = cityRepository.findById(dto.cityId())
                .orElseThrow(() -> new AppException("City not found", HttpStatus.NOT_FOUND));

        Factory factory = new Factory();
        applyDto(factory, dto, country, city);
        return toDto(factoryRepository.save(factory));
    }

    @Transactional
    public FactoryDto update(Long id, FactoryRequestDto dto) {
        Factory factory = findOrThrow(id);
        Country country = countryRepository.findById(dto.countryId())
                .orElseThrow(() -> new AppException("Country not found", HttpStatus.NOT_FOUND));
        City city = cityRepository.findById(dto.cityId())
                .orElseThrow(() -> new AppException("City not found", HttpStatus.NOT_FOUND));

        factory.getImages().clear();
        applyDto(factory, dto, country, city);
        return toDto(factoryRepository.save(factory));
    }

    @Transactional
    public void delete(Long id) {
        if (!factoryRepository.existsById(id)) {
            throw new AppException("Factory not found", HttpStatus.NOT_FOUND);
        }
        factoryRepository.deleteById(id);
    }

    // ─── Lookup ──────────────────────────────────────────────────────────────

    public List<CountryDto> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(c -> new CountryDto(c.getId(), c.getName(), c.getCode()))
                .toList();
    }

    public List<CityDto> getCitiesByCountry(Long countryId) {
        return cityRepository.findByCountryId(countryId).stream()
                .map(c -> new CityDto(c.getId(), c.getName(), c.getCountry().getId()))
                .toList();
    }

    // ─── Heartbeat checker ───────────────────────────────────────────────────

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void checkHeartbeats() {
        Instant cutoff = Instant.now().minusSeconds(60);
        List<Factory> stale = factoryRepository.findStaleOnlineFactoriesForUpdate(cutoff);
        for (Factory f : stale) {
            f.setOnline(false);
            influxDBService.writeOfflineEvent(f.getId());
        }
        if (!stale.isEmpty()) {
            factoryRepository.saveAll(stale);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Factory findOrThrow(Long id) {
        return factoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Factory not found", HttpStatus.NOT_FOUND));
    }

    private void applyDto(Factory factory, FactoryRequestDto dto, Country country, City city) {
        factory.setName(dto.name());
        factory.setAddress(dto.address());
        factory.setCountry(country);
        factory.setCity(city);
        factory.setLatitude(dto.latitude());
        factory.setLongitude(dto.longitude());

        if (dto.imageKeys() != null) {
            for (String key : dto.imageKeys()) {
                FactoryImage img = new FactoryImage();
                img.setImageKey(key);
                img.setFactory(factory);
                factory.getImages().add(img);
            }
        }

        factory.getProducts().clear();
        if (dto.productIds() != null && !dto.productIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(dto.productIds());
            factory.setProducts(products);
        }
    }

    public FactoryDto toDto(Factory f) {
        List<String> imageKeys = f.getImages().stream().map(FactoryImage::getImageKey).toList();
        List<Long> productIds = f.getProducts().stream().map(Product::getId).toList();
        return new FactoryDto(
                f.getId(), f.getName(), f.getAddress(),
                new CountryDto(f.getCountry().getId(), f.getCountry().getName(), f.getCountry().getCode()),
                new CityDto(f.getCity().getId(), f.getCity().getName(), f.getCity().getCountry().getId()),
                f.getLatitude(), f.getLongitude(),
                f.isOnline(), f.getLastHeartbeat() != null ? f.getLastHeartbeat().toString() : null,
                imageKeys, productIds
        );
    }
}