package com.smartcompany.model.company;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "company_document",
    // a company's documents are loaded by company_id for every company in a list
    indexes = @Index(name = "idx_company_document_company", columnList = "company_id")
)
@NoArgsConstructor @AllArgsConstructor @Data
public class CompanyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String fileKey;

    @Column(nullable = false)
    private String fileType; // "image" ili "pdf"
}