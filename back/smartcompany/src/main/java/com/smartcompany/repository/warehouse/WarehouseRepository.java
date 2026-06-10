package com.smartcompany.repository.warehouse;

import com.smartcompany.model.warehouse.Warehouse;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> 
{
        @Query("SELECT w FROM Warehouse w WHERE w.isOnline = true AND w.lastHeartbeat < :cutoff")
        List<Warehouse> findStaleOnlineWarehouses(@Param("cutoff") Instant cutoff);
}