package com.smartcompany.repository.warehouse;

import com.smartcompany.model.warehouse.WarehouseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarehouseItemRepository extends JpaRepository<WarehouseItem, Long> {

    // Ukupna zaliha nekog proizvoda u svim magacinima
    @Query("SELECT COALESCE(SUM(wi.quantity), 0) FROM WarehouseItem wi WHERE wi.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    // Sve stavke za dati proizvod, zaključane za update (pesimistično zaključavanje)
    @Query("SELECT wi FROM WarehouseItem wi WHERE wi.product.id = :productId AND wi.quantity > 0 ORDER BY wi.quantity DESC")
    List<WarehouseItem> findByProductIdWithStock(@Param("productId") Long productId);

    Optional<WarehouseItem> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}