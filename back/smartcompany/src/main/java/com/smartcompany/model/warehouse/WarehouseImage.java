package com.smartcompany.model.warehouse;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "warehouse_image",
    // a warehouse's images are loaded by warehouse_id for every warehouse in a list
    indexes = @Index(name = "idx_warehouse_image_warehouse", columnList = "warehouse_id")
)
@NoArgsConstructor @AllArgsConstructor @Data
public class WarehouseImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private String imageKey;
}