package com.smartcompany.model.vehicle;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "vehicle_image",
    // a vehicle's images are loaded by vehicle_id for every vehicle in a list
    indexes = @Index(name = "idx_vehicle_image_vehicle", columnList = "vehicle_id")
)
@NoArgsConstructor @AllArgsConstructor @Data
public class VehicleImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private String imageKey;
}