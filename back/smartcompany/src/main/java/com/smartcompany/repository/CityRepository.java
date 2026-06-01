package com.smartcompany.repository;

import com.smartcompany.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountryId(Long countryId);
}