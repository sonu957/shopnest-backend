package com.shopnest.controller;

import com.shopnest.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getCart(Authentication auth) {
        List<Map<String, Object>> items = cartService.getCartItems(auth.getName());
        return ResponseEntity.ok(Map.of("success", true, "data", items));
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartRequest request, Authentication auth) {
        cartService.addToCart(auth.getName(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(Map.of("success", true, "message", "Item added to cart"));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestParam Integer quantity,
                                        Authentication auth) {
        cartService.updateCartItem(auth.getName(), itemId, quantity);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cart updated"));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long itemId, Authentication auth) {
        cartService.removeFromCart(auth.getName(), itemId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Item removed"));
    }

    @Data
    static class CartRequest {
        @NotNull private Long productId;
        @NotNull @Min(1) private Integer quantity;
    }
}
