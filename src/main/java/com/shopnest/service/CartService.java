package com.shopnest.service;

import com.shopnest.entity.CartItem;
import com.shopnest.entity.Product;
import com.shopnest.entity.User;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.CartItemRepository;
import com.shopnest.repository.ProductRepository;
import com.shopnest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Map<String, Object>> getCartItems(String email) {
        User user = getUserByEmail(email);
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (CartItem item : items) {
            result.add(toMap(item));
        }
        return result;
    }

    @Transactional
    public CartItem addToCart(String email, Long productId, Integer quantity) {
        User user = getUserByEmail(email);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQty() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQty());
        }

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(user.getId(), productId);
        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartItemRepository.save(cartItem);
        }

        CartItem cartItem = CartItem.builder()
            .user(user).product(product).quantity(quantity).build();
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public CartItem updateCartItem(String email, Long itemId, Integer quantity) {
        User user = getUserByEmail(email);
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(String email, Long itemId) {
        User user = getUserByEmail(email);
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Map<String, Object> toMap(CartItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("productId", item.getProduct().getId());
        map.put("productName", item.getProduct().getName());
        map.put("productPrice", item.getProduct().getPrice());
        map.put("productImageUrl", item.getProduct().getImageUrl());
        map.put("quantity", item.getQuantity());
        map.put("subtotal", item.getProduct().getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())));
        return map;
    }
}
