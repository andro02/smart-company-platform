package com.smartcompany.service.order;

import com.smartcompany.dto.company.CompanyDto;
import com.smartcompany.dto.order.CreateOrderDto;
import com.smartcompany.dto.order.OrderResponseDto;
import com.smartcompany.exception.AppException;
import com.smartcompany.model.*;
import com.smartcompany.model.company.Company;
import com.smartcompany.model.company.CompanyStatus;
import com.smartcompany.model.order.Order;
import com.smartcompany.model.order.OrderItem;
import com.smartcompany.model.warehouse.WarehouseItem;
import com.smartcompany.repository.*;
import com.smartcompany.repository.company.CompanyRepository;
import com.smartcompany.repository.order.OrderRepository;
import com.smartcompany.repository.warehouse.WarehouseItemRepository;
import com.smartcompany.service.EmailService;
import com.smartcompany.service.PdfService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseItemRepository warehouseItemRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PdfService pdfService;
    private final EmailService emailService;

    public List<CompanyDto> getMyCompanies(String username) {
        return companyRepository
            .findByOwnerUsernameAndStatus(username, CompanyStatus.APPROVED)
            .stream()
            .map(c -> new CompanyDto(
                c.getId(),
                c.getName(),
                c.getCity().getName(),
                c.getCountry().getName()))
            .toList();
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", allEntries = true)
    })
    public OrderResponseDto createOrder(String customerUsername, CreateOrderDto dto) {
        User customer = userRepository.findById(customerUsername)
            .orElseThrow(() -> new AppException("Korisnik nije pronađen", HttpStatus.NOT_FOUND));

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> new AppException("Firma nije pronađena", HttpStatus.NOT_FOUND));

        if (!company.getOwner().getUsername().equals(customerUsername)) {
            throw new AppException("Firma ne pripada ovom korisniku", HttpStatus.FORBIDDEN);
        }

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new AppException("Firma nije odobrena", HttpStatus.BAD_REQUEST);
        }

        // Sortiraj stavke po productId — svaka transakcija tada zaključava
        // proizvode u istom redosledu, što sprečava deadlock kada dve
        // porudžbine istovremeno traže iste proizvode u različitom redosledu.
        List<CreateOrderDto.OrderItemDto> sortedItems = dto.items().stream()
            .sorted(Comparator.comparing(CreateOrderDto.OrderItemDto::productId))
            .toList();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        try {
            for (CreateOrderDto.OrderItemDto itemDto : sortedItems) {
                Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new AppException(
                        "Proizvod nije pronađen: " + itemDto.productId(), HttpStatus.NOT_FOUND));

                if (!product.isForSale()) {
                    throw new AppException("Proizvod nije na prodaji: " + product.getName(),
                        HttpStatus.BAD_REQUEST);
                }

                // PESIMISTIČKO ZAKLJUČAVANJE: findByProductIdWithStock sada radi
                // SELECT ... FOR UPDATE nad svim WarehouseItem redovima ovog
                // proizvoda. Druga transakcija koja traži isti proizvod čeka dok
                // ova ne commit-uje/rollback-uje, pa je totalAvailable ispod
                // uvek tačan — nema više lost update-a nad zalihama.
                List<WarehouseItem> warehouseItems =
                    warehouseItemRepository.findByProductIdWithStock(product.getId());

                int totalAvailable = warehouseItems.stream()
                    .mapToInt(WarehouseItem::getQuantity).sum();

                if (totalAvailable < itemDto.quantity()) {
                    throw new AppException(
                        "Nedovoljno zaliha za: " + product.getName() +
                        " (traženo: " + itemDto.quantity() + ", dostupno: " + totalAvailable + ")",
                        HttpStatus.CONFLICT
                    );
                }

                int remaining = itemDto.quantity();
                for (WarehouseItem wi : warehouseItems) {
                    if (remaining <= 0) break;
                    int deduct = Math.min(wi.getQuantity(), remaining);
                    wi.setQuantity(wi.getQuantity() - deduct);
                    warehouseItemRepository.save(wi);
                    remaining -= deduct;
                }

                OrderItem oi = new OrderItem();
                oi.setProduct(product);
                oi.setQuantity(itemDto.quantity());
                oi.setPriceAtOrder(product.getPrice());
                orderItems.add(oi);

                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity())));
            }
        } catch (PessimisticLockingFailureException e) {
            // Nije uspelo zaključavanje na vreme (npr. druga porudžbina predugo
            // drži iste redove) — javi korisniku da pokuša ponovo umesto 500-ke.
            throw new AppException(
                "Sistem je trenutno zauzet obradom porudžbina za ove proizvode, pokušajte ponovo",
                HttpStatus.CONFLICT
            );
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setCompany(company);
        order.setCreatedAt(Instant.now());
        order.setTotalPrice(total);
        order.setItems(orderItems);
        orderItems.forEach(oi -> oi.setOrder(order));

        Order saved = orderRepository.save(order);

        byte[] pdf = pdfService.generateInvoice(saved);
        emailService.sendInvoice(customer.getEmail(), pdf, saved.getId());

        return toDto(saved);
    }

    private OrderResponseDto toDto(Order order) {
        return new OrderResponseDto(
            order.getId(),
            order.getCreatedAt().toString(),
            order.getTotalPrice(),
            order.getCompany().getId(),
            order.getCompany().getName(),
            order.getItems().stream().map(oi -> new OrderResponseDto.OrderItemResponseDto(
                oi.getProduct().getId(),
                oi.getProduct().getName(),
                oi.getQuantity(),
                oi.getPriceAtOrder()
            )).toList()
        );
    }
}