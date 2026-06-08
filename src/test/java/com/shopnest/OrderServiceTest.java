package com.shopnest;

import com.shopnest.entity.*;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.*;
import com.shopnest.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PaymentRepository paymentRepository;

    private OrderService orderService;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Manually construct to avoid @InjectMocks issues with @RequiredArgsConstructor
        orderService = new OrderService(
            orderRepository,
            orderItemRepository,
            cartItemRepository,
            userRepository,
            productRepository,
            paymentRepository
        );

        testUser = User.builder()
            .id(1L).name("Test User").email("test@test.com")
            .role(User.Role.CUSTOMER).build();

        testProduct = Product.builder()
            .id(1L).name("Test Product")
            .price(new BigDecimal("999.00"))
            .stockQty(10).isActive(true).build();
    }

    @Test
    void placeOrder_WithValidCart_ShouldSucceed() {
        CartItem cartItem = CartItem.builder()
            .id(1L).user(testUser).product(testProduct).quantity(2).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o = Order.builder().id(1L).user(testUser)
                .totalAmount(new BigDecimal("1998.00"))
                .status(Order.OrderStatus.PLACED)
                .address("123 Test Street").build();
            return o;
        });
        when(paymentRepository.save(any())).thenReturn(Payment.builder()
            .status(Payment.PaymentStatus.SUCCESS).build());

        Map<String, Object> result = orderService.placeOrder("test@test.com", "123 Test Street", "CARD");

        assertNotNull(result);
        verify(cartItemRepository).deleteByUserId(1L);
        verify(productRepository).save(testProduct);
    }

    @Test
    void placeOrder_WithEmptyCart_ShouldThrowException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            orderService.placeOrder("test@test.com", "address", "CARD"));

        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void placeOrder_WithInsufficientStock_ShouldThrowException() {
        testProduct.setStockQty(1); // Only 1 in stock
        CartItem cartItem = CartItem.builder()
            .user(testUser).product(testProduct).quantity(5).build(); // Wants 5

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            orderService.placeOrder("test@test.com", "address", "CARD"));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void updateOrderStatus_ValidTransition_ShouldSucceed() {
        Order order = Order.builder().id(1L).status(Order.OrderStatus.PLACED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        Order updated = orderService.updateOrderStatus(1L, "CONFIRMED");

        assertEquals(Order.OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void updateOrderStatus_InvalidTransition_ShouldThrowException() {
        Order order = Order.builder().id(1L).status(Order.OrderStatus.DELIVERED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            orderService.updateOrderStatus(1L, "PLACED"));

        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }

    @Test
    void getOrderById_WithWrongUser_ShouldThrowUnauthorized() {
        Order order = Order.builder()
            .id(1L).user(testUser).status(Order.OrderStatus.PLACED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            orderService.getOrderById(1L, "other@user.com"));

        assertEquals("Unauthorized to view this order", ex.getMessage());
    }
}
