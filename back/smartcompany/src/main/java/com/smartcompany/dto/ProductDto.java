package com.smartcompany.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Double weight;
    private String image;
    private boolean isForSale;
    private CategoryDto category;
    private Integer totalStock;
    private List<FactorySummaryDto> factories;
}