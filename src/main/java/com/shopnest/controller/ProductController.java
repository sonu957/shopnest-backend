package com.shopnest.controller;

import com.shopnest.entity.Product;
import com.shopnest.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<Product> products = productService.getAllProducts(page, size, sortBy);
        return buildPageResponse(products);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Product> products = productService.searchProducts(keyword, page, size);
        return buildPageResponse(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Product> products = productService.getProductsByCategory(categoryId, page, size);
        return buildPageResponse(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(Map.of("success", true, "data", productService.toMap(product)));
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(
            request.getName(), request.getDescription(), request.getPrice(),
            request.getStockQty(), request.getImageUrl(), request.getCategoryId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Product created", "data", productService.toMap(product)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product product = productService.updateProduct(
            id, request.getName(), request.getDescription(), request.getPrice(),
            request.getStockQty(), request.getImageUrl(), request.getCategoryId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Product updated", "data", productService.toMap(product)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Product deleted"));
    }

    private ResponseEntity<?> buildPageResponse(Page<Product> products) {
        List<Map<String, Object>> content = products.getContent()
            .stream().map(productService::toMap).collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", content);
        response.put("totalElements", products.getTotalElements());
        response.put("totalPages", products.getTotalPages());
        response.put("currentPage", products.getNumber());
        return ResponseEntity.ok(response);
    }

    @Data
    static class ProductRequest {
        @NotBlank private String name;
        private String description;
        @NotNull @DecimalMin("0.01") private BigDecimal price;
        @NotNull @Min(0) private Integer stockQty;
        private String imageUrl;
        @NotNull private Long categoryId;
    }
}
