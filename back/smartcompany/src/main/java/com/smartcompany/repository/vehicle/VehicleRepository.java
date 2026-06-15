package com.smartcompany.repository.vehicle;

import com.smartcompany.model.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>,
        JpaSpecificationExecutor<Vehicle> {

    @Query("SELECT v FROM Vehicle v WHERE v.isOnline = true AND v.lastHeartbeat < :cutoff")
    List<Vehicle> findStaleOnlineVehicles(@Param("cutoff") Instant cutoff);
}