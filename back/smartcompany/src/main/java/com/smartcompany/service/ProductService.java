package com.smartcompany.service;

import com.smartcompany.dto.CategoryDto;
import com.smartcompany.dto.FactorySummaryDto;
import com.smartcompany.dto.ProductDto;
import com.smartcompany.dto.ProductRequestDto;
import com.smartcompany.exception.AppException;
import com.smartcompany.model.Category;
import com.smartcompany.model.Product;
import com.smartcompany.repository.CategoryRepository;
import com.smartcompany.repository.FactoryRepository;
import com.smartcompany.repository.ProductRepository;
import com.smartcompany.repository.warehouse.WarehouseItemRepository;
import com.smartcompany.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FactoryRepository factoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WarehouseItemRepository warehouseItemRepository;

    @Cacheable(value = "products",
        key = "#search + '-' + #categoryId + '-' + #isForSale + '-' + #minPrice + '-' + #maxPrice + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<ProductDto> search(String search, Long categoryId, Boolean isForSale,
                                    BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.search(search, categoryId, isForSale, minPrice, maxPrice);
        return productRepository.findAll(spec, pageable).map(this::toDto);
    }

    @Cacheable(value = "product", key = "#id")
    public ProductDto getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
        return toDtoWithFactories(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDto create(ProductRequestDto dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));

        Product product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setWeight(dto.weight());
        product.setImage(dto.image());
        product.setForSale(dto.isForSale());
        product.setCategory(category);

        Product saved = productRepository.save(product);

        return toDto(saved);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", key = "#id")
    })
    public ProductDto update(Long id, ProductRequestDto dto) {
        Product product;
        try {
            product = productRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
        } catch (PessimisticLockingFailureException e) {
            throw new AppException(
                "Proizvod se trenutno menja od strane drugog menadžera, pokušajte ponovo",
                HttpStatus.CONFLICT
            );
        }

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setWeight(dto.weight());
        product.setImage(dto.image());
        product.setForSale(dto.isForSale());
        product.setCategory(category);

        Product saved = productRepository.save(product);

        return toDto(saved);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "product", key = "#id")
    })
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new AppException("Product not found", HttpStatus.NOT_FOUND);
        }
        productRepository.deleteById(id);
    }

    public ProductDto toDto(Product product) {
        CategoryDto categoryDto = categoryService.toDto(product.getCategory());
        Integer totalStock = warehouseItemRepository.sumQuantityByProductId(product.getId());

        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getWeight(),
                product.getImage(),
                product.isForSale(),
                categoryDto,
                totalStock,
                null
        );
    }

    public ProductDto toDtoWithFactories(Product product) {
        ProductDto base = toDto(product);
        List<FactorySummaryDto> factories = factoryRepository.findByProductId(product.getId()).stream()
                .map(f -> new FactorySummaryDto(f.getId(), f.getName()))
                .toList();
        base.setFactories(factories);
        return base;
    }

}