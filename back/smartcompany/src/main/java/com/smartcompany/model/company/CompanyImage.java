package com.smartcompany.model.company;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "company_image",
    // a company's images are loaded by company_id for every company in a list
    indexes = @Index(name = "idx_company_image_company", columnList = "company_id")
)
@NoArgsConstructor @AllArgsConstructor @Data
public class CompanyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String imageKey;
}