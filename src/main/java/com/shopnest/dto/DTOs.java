package com.shopnest.dto;

import com.shopnest.entity.Order;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ========== AUTH DTOs ==========

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Valid email required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class LoginRequest {
    @Email @NotBlank private String email;
    @NotBlank private String password;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class AuthResponse {
    private String token;
    private String email;
    private String name;
    private String role;
}

// ========== PRODUCT DTOs ==========

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class ProductRequest {
    @NotBlank private String name;
    private String description;
    @NotNull @DecimalMin("0.01") private BigDecimal price;
    @NotNull @Min(0) private Integer stockQty;
    private String imageUrl;
    @NotNull private Long categoryId;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String imageUrl;
    private String categoryName;
    private LocalDateTime createdAt;
}

// ========== CART DTOs ==========

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class CartItemRequest {
    @NotNull private Long productId;
    @NotNull @Min(1) private Integer quantity;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal subtotal;
}

// ========== ORDER DTOs ==========

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class OrderRequest {
    @NotBlank private String address;
    private String paymentMethod;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class OrderResponse {
    private Long id;
    private BigDecimal totalAmount;
    private String status;
    private String address;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private String paymentStatus;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}

// ========== CATEGORY DTOs ==========

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class CategoryRequest {
    @NotBlank private String name;
    private String description;
}

// ========== GENERIC RESPONSE ==========

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }
}
