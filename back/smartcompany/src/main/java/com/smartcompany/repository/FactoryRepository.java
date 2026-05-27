package com.smartcompany.repository;

import com.smartcompany.model.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface FactoryRepository extends JpaRepository<Factory, Long>, JpaSpecificationExecutor<Factory> {

    // Sve fabrike koje su online ali im je heartbeat stariji od datog trenutka
    // koristi @Scheduled heartbeat checker
    @Query("SELECT f FROM Factory f WHERE f.isOnline = true AND (f.lastHeartbeat IS NULL OR f.lastHeartbeat < :cutoff)")
    List<Factory> findStaleOnlineFactories(Instant cutoff);
}