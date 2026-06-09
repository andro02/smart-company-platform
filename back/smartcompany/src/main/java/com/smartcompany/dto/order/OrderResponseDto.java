package com.smartcompany.dto.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
    Long id,
    String createdAt,
    BigDecimal totalPrice,
    Long companyId,
    String companyName,
    List<OrderItemResponseDto> items
) {
    public record OrderItemResponseDto(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtOrder
    ) {}
}