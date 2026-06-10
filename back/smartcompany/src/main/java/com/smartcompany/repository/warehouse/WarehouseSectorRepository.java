package com.smartcompany.repository.warehouse;

import com.smartcompany.model.warehouse.WarehouseSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseSectorRepository extends JpaRepository<WarehouseSector, Long> {
    List<WarehouseSector> findByWarehouseId(Long warehouseId);
}