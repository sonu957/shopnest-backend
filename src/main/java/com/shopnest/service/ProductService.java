package com.shopnest.service;

import com.shopnest.entity.Category;
import com.shopnest.entity.Product;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.CategoryRepository;
import com.shopnest.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<Product> getAllProducts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return productRepository.findByIsActiveTrue(pageable);
    }

    public Page<Product> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchProducts(keyword, pageable);
    }

    public Page<Product> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Product createProduct(String name, String description, BigDecimal price,
                                 Integer stockQty, String imageUrl, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        Product product = Product.builder()
            .name(name)
            .description(description)
            .price(price)
            .stockQty(stockQty)
            .imageUrl(imageUrl)
            .category(category)
            .isActive(true)
            .build();

        return productRepository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Product updateProduct(Long id, String name, String description, BigDecimal price,
                                  Integer stockQty, String imageUrl, Long categoryId) {
        Product product = getProductById(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQty(stockQty);
        product.setImageUrl(imageUrl);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setIsActive(false); // Soft delete
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
    }

    public Map<String, Object> toMap(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("description", p.getDescription());
        map.put("price", p.getPrice());
        map.put("stockQty", p.getStockQty());
        map.put("imageUrl", p.getImageUrl());
        map.put("categoryName", p.getCategory() != null ? p.getCategory().getName() : null);
        map.put("categoryId", p.getCategory() != null ? p.getCategory().getId() : null);
        map.put("createdAt", p.getCreatedAt());
        return map;
    }
}
