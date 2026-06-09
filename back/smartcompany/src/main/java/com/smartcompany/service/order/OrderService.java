package com.smartcompany.service.order;

import com.smartcompany.dto.company.CompanyDto;
import com.smartcompany.dto.order.CreateOrderDto;
import com.smartcompany.dto.order.OrderResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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

    // Dohvati firme kupca (samo APPROVED)
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
    public OrderResponseDto createOrder(String customerUsername, CreateOrderDto dto) {
        User customer = userRepository.findById(customerUsername)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> new RuntimeException("Firma nije pronađena"));

        // Proveri da firma pripada kupcu
        if (!company.getOwner().getUsername().equals(customerUsername)) {
            throw new RuntimeException("Firma ne pripada ovom korisniku");
        }

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new RuntimeException("Firma nije odobrena");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderDto.OrderItemDto itemDto : dto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                .orElseThrow(() -> new RuntimeException("Proizvod nije pronađen: " + itemDto.productId()));

            if (!product.isForSale()) {
                throw new RuntimeException("Proizvod nije na prodaji: " + product.getName());
            }

            // Smanji zalihe raspoređeno po magacinima (od najzasupljenijeg)
            int remaining = itemDto.quantity();
            List<WarehouseItem> warehouseItems =
                warehouseItemRepository.findByProductIdWithStock(product.getId());

            int totalAvailable = warehouseItems.stream()
                .mapToInt(WarehouseItem::getQuantity).sum();

            if (totalAvailable < itemDto.quantity()) {
                throw new RuntimeException(
                    "Nedovoljno zaliha za: " + product.getName() +
                    " (traženo: " + itemDto.quantity() + ", dostupno: " + totalAvailable + ")"
                );
            }

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

        Order order = new Order();
        order.setCustomer(customer);
        order.setCompany(company);
        order.setCreatedAt(Instant.now());
        order.setTotalPrice(total);
        order.setItems(orderItems);
        orderItems.forEach(oi -> oi.setOrder(order));

        Order saved = orderRepository.save(order);

        // Generiši PDF fakturu i pošalji email
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