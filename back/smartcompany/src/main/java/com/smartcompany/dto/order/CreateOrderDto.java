package com.smartcompany.dto.order;

import java.util.List;

public record CreateOrderDto(
    Long companyId,
    List<OrderItemDto> items
) {
    public record OrderItemDto(Long productId, Integer quantity) {}
}