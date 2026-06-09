package com.smartcompany.repository.warehouse;

import com.smartcompany.model.warehouse.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long>,
        JpaSpecificationExecutor<Warehouse> {
}