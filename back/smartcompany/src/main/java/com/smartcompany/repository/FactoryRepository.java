package com.smartcompany.repository;

import com.smartcompany.model.Factory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface FactoryRepository extends JpaRepository<Factory, Long>, JpaSpecificationExecutor<Factory> {

    @Query("SELECT f FROM Factory f WHERE f.isOnline = true AND (f.lastHeartbeat IS NULL OR f.lastHeartbeat < :cutoff)")
    List<Factory> findStaleOnlineFactories(Instant cutoff);

    @Query("SELECT f FROM Factory f JOIN f.products p WHERE p.id = :productId")
    List<Factory> findByProductId(@Param("productId") Long productId);

    // Zaključava jednu fabriku — koristi se u handleHeartbeat() da spreči
    // lost update sa checkHeartbeats().
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT f FROM Factory f WHERE f.id = :id")
    Optional<Factory> findByIdForUpdate(@Param("id") Long id);

    // Zaključana varijanta findStaleOnlineFactories — koristi se u checkHeartbeats().
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT f FROM Factory f WHERE f.isOnline = true AND (f.lastHeartbeat IS NULL OR f.lastHeartbeat < :cutoff)")
    List<Factory> findStaleOnlineFactoriesForUpdate(Instant cutoff);
}