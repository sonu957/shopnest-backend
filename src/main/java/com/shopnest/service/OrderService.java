package com.shopnest.service;

import com.shopnest.entity.*;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Map<String, Object> placeOrder(String email, String address, String paymentMethod) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            if (item.getProduct().getStockQty() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + item.getProduct().getName());
            }
            totalAmount = totalAmount.add(
                item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        Order order = Order.builder()
            .user(user)
            .totalAmount(totalAmount)
            .status(Order.OrderStatus.PLACED)
            .address(address)
            .build();
        orderRepository.save(order);

        for (CartItem item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(item.getProduct())
                .quantity(item.getQuantity())
                .price(item.getProduct().getPrice())
                .build();
            orderItemRepository.save(orderItem);

            Product product = item.getProduct();
            product.setStockQty(product.getStockQty() - item.getQuantity());
            productRepository.save(product);
        }

        Payment payment = Payment.builder()
            .order(order)
            .amount(totalAmount)
            .status(Payment.PaymentStatus.SUCCESS)
            .paidAt(LocalDateTime.now())
            .paymentMethod(paymentMethod != null ? paymentMethod : "CARD")
            .build();
        paymentRepository.save(payment);

        cartItemRepository.deleteByUserId(user.getId());

        log.info("Order placed: {} for user: {}", order.getId(), email);
        return toOrderMap(order, payment);
    }

    public Page<Order> getMyOrders(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));
    }

    public Order getOrderById(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to view this order");
        }
        return order;
    }

    /**
     * CANCEL ORDER — customer cancels their own order
     * Only PLACED or CONFIRMED orders can be cancelled
     * Stock is restored automatically on cancellation
     */
    @Transactional
    public Order cancelOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Ownership check
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        // Status check — only PLACED or CONFIRMED can be cancelled
        if (order.getStatus() != Order.OrderStatus.PLACED &&
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException(
                "Cannot cancel. Only PLACED or CONFIRMED orders can be cancelled. Current status: "
                + order.getStatus()
            );
        }

        // Restore stock for each item
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStockQty(product.getStockQty() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        log.info("Order {} cancelled by: {}", orderId, email);
        return orderRepository.save(order);
    }

    /**
     * ORDER STATE MACHINE — Admin only
     * PLACED -> CONFIRMED -> SHIPPED -> DELIVERED
     * Any -> CANCELLED
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Order.OrderStatus current = order.getStatus();
        Order.OrderStatus next = Order.OrderStatus.valueOf(newStatus);

        validateStatusTransition(current, next);

        order.setStatus(next);
        log.info("Order {} status: {} -> {}", orderId, current, next);
        return orderRepository.save(order);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<Order> getAllOrders(int page, int size) {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        Map<Order.OrderStatus, List<Order.OrderStatus>> validTransitions = new HashMap<>();
        validTransitions.put(Order.OrderStatus.PLACED,
            Arrays.asList(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED));
        validTransitions.put(Order.OrderStatus.CONFIRMED,
            Arrays.asList(Order.OrderStatus.SHIPPED, Order.OrderStatus.CANCELLED));
        validTransitions.put(Order.OrderStatus.SHIPPED,
            List.of(Order.OrderStatus.DELIVERED));
        validTransitions.put(Order.OrderStatus.DELIVERED, Collections.emptyList());
        validTransitions.put(Order.OrderStatus.CANCELLED, Collections.emptyList());

        List<Order.OrderStatus> allowed = validTransitions.getOrDefault(current, Collections.emptyList());
        if (!allowed.contains(next)) {
            throw new RuntimeException(
                "Invalid status transition: " + current + " -> " + next
            );
        }
    }

    public Map<String, Object> toOrderMap(Order order, Payment payment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("totalAmount", order.getTotalAmount());
        map.put("status", order.getStatus().name());
        map.put("address", order.getAddress());
        map.put("createdAt", order.getCreatedAt());
        map.put("paymentStatus", payment != null ? payment.getStatus().name() : "N/A");
        return map;
    }
}
