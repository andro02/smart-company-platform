package com.smartcompany.controller.order;

import com.smartcompany.dto.company.CompanyDto;
import com.smartcompany.dto.order.CreateOrderDto;
import com.smartcompany.dto.order.OrderResponseDto;
import com.smartcompany.dto.CategoryDto;
import com.smartcompany.dto.ProductDto;
import com.smartcompany.dto.UserDto;
import com.smartcompany.service.CategoryService;
import com.smartcompany.service.ProductService;
import com.smartcompany.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final CategoryService categoryService;

    // Moje firme (za dropdown u formi)
    @GetMapping("/my-companies")
    @PreAuthorize("hasRole('Customer')")
    public ResponseEntity<List<CompanyDto>> getMyCompanies(
            @AuthenticationPrincipal UserDto user) {
        return ResponseEntity.ok(orderService.getMyCompanies(user.getUsername()));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('Customer')")
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        var pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.search(name, categoryId, true, minPrice, maxPrice, pageable));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('Customer')")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    // Kreiranje porudžbine
    @PostMapping
    @PreAuthorize("hasRole('Customer')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @AuthenticationPrincipal UserDto user,
            @RequestBody CreateOrderDto dto) {
        return ResponseEntity.ok(orderService.createOrder(user.getUsername(), dto));
    }
}