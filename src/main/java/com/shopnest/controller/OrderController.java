//package com.shopnest.controller;
//
//import com.shopnest.entity.Order;
//import com.shopnest.service.OrderService;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotBlank;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/orders")
//@RequiredArgsConstructor
//public class OrderController {
//
//    private final OrderService orderService;
//
//    @PostMapping
//    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest request, Authentication auth) {
//        Map<String, Object> order = orderService.placeOrder(
//            auth.getName(), request.getAddress(), request.getPaymentMethod());
//        return ResponseEntity.ok(Map.of("success", true, "message", "Order placed successfully!", "data", order));
//    }
//
//    @GetMapping
//    public ResponseEntity<?> getMyOrders(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Authentication auth) {
//        Page<Order> orders = orderService.getMyOrders(auth.getName(), page, size);
//        List<Map<String, Object>> data = orders.getContent()
//            .stream().map(this::toSafeMap).collect(Collectors.toList());
//        return ResponseEntity.ok(Map.of(
//            "success", true,
//            "data", data,
//            "totalElements", orders.getTotalElements(),
//            "totalPages", orders.getTotalPages()
//        ));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getOrder(@PathVariable Long id, Authentication auth) {
//        Order order = orderService.getOrderById(id, auth.getName());
//        return ResponseEntity.ok(Map.of("success", true, "data", toSafeMap(order)));
//    }
//
//    // Cancel order — customer cancels their own PLACED or CONFIRMED order
//    @PatchMapping("/{id}/cancel")
//    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Authentication auth) {
//        Order order = orderService.cancelOrder(id, auth.getName());
//        return ResponseEntity.ok(Map.of(
//            "success", true,
//            "message", "Order cancelled successfully. Stock has been restored.",
//            "data", toSafeMap(order)
//        ));
//    }
//
//    // Admin endpoints
//    @GetMapping("/admin/all")
//    public ResponseEntity<?> getAllOrders(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Page<Order> orders = orderService.getAllOrders(page, size);
//        List<Map<String, Object>> data = orders.getContent()
//            .stream().map(this::toAdminMap).collect(Collectors.toList());
//        return ResponseEntity.ok(Map.of("success", true, "data", data,
//            "totalElements", orders.getTotalElements()));
//    }
//
//    @PatchMapping("/admin/{id}/status")
//    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
//        Order order = orderService.updateOrderStatus(id, status);
//        return ResponseEntity.ok(Map.of("success", true, "message", "Status updated", "data", toSafeMap(order)));
//    }
//
//    // Converts Order to safe Map — avoids circular reference (Order->User->Orders->...)
//    private Map<String, Object> toSafeMap(Order order) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("id", order.getId());
//        map.put("totalAmount", order.getTotalAmount());
//        map.put("status", order.getStatus().name());
//        map.put("address", order.getAddress());
//        map.put("createdAt", order.getCreatedAt());
//
//        if (order.getOrderItems() != null) {
//            List<Map<String, Object>> items = order.getOrderItems().stream().map(item -> {
//                Map<String, Object> i = new HashMap<>();
//                i.put("productName", item.getProduct() != null ? item.getProduct().getName() : "");
//                i.put("quantity", item.getQuantity());
//                i.put("price", item.getPrice());
//                return i;
//            }).collect(Collectors.toList());
//            map.put("items", items);
//        }
//
//        if (order.getPayment() != null) {
//            map.put("paymentStatus", order.getPayment().getStatus().name());
//        }
//        return map;
//    }
//
//    private Map<String, Object> toAdminMap(Order order) {
//        Map<String, Object> map = toSafeMap(order);
//        if (order.getUser() != null) {
//            map.put("userEmail", order.getUser().getEmail());
//            map.put("userName", order.getUser().getName());
//        }
//        return map;
//    }
//
//    @Data
//    static class OrderRequest {
//        @NotBlank private String address;
//        private String paymentMethod;
//    }
//}


package com.shopnest.controller;

import com.shopnest.entity.Order;
import com.shopnest.entity.OrderItem;
import com.shopnest.entity.Payment;
import com.shopnest.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication auth) {
        try {
            Map<String, Object> order = orderService.placeOrder(
                auth.getName(), request.getAddress(), request.getPaymentMethod());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order placed successfully!",
                "data", order
            ));
        } catch (Exception e) {
            log.error("Place order error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        try {
            Page<Order> orders = orderService.getMyOrders(auth.getName(), page, size);
            List<Map<String, Object>> data = orders.getContent()
                .stream()
                .map(this::toSafeMap)
                .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data,
                "totalElements", orders.getTotalElements(),
                "totalPages", orders.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Get orders error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "data", Collections.emptyList()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long id,
            Authentication auth) {
        try {
            Order order = orderService.getOrderById(id, auth.getName());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", toSafeMap(order)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            Authentication auth) {
        try {
            Order order = orderService.cancelOrder(id, auth.getName());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order cancelled successfully. Stock has been restored.",
                "data", toSafeMap(order)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ===== ADMIN ENDPOINTS =====

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Order> orders = orderService.getAllOrders(page, size);
            List<Map<String, Object>> data = orders.getContent()
                .stream()
                .map(this::toAdminMap)
                .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data,
                "totalElements", orders.getTotalElements()
            ));
        } catch (Exception e) {
            log.error("Get all orders error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "data", Collections.emptyList()
            ));
        }
    }

    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Status updated to " + status,
                "data", toSafeMap(order)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ===== SAFE MAP CONVERTERS =====
    // Converts Order entity to plain Map to avoid circular reference
    // Order -> User -> List<Order> -> User -> ... (infinite loop)

    private Map<String, Object> toSafeMap(Order order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("totalAmount", order.getTotalAmount());
        map.put("status", order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");
        map.put("address", order.getAddress() != null ? order.getAddress() : "");
        map.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");

        // Safe order items
        List<Map<String, Object>> items = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                try {
                    Map<String, Object> itemMap = new LinkedHashMap<>();
                    itemMap.put("productName", item.getProduct() != null ? item.getProduct().getName() : "Unknown");
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice());
                    BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    itemMap.put("subtotal", subtotal);
                    items.add(itemMap);
                } catch (Exception e) {
                    log.warn("Could not map order item: {}", e.getMessage());
                }
            }
        }
        map.put("items", items);

        // Safe payment status
        try {
            Payment payment = order.getPayment();
            map.put("paymentStatus", payment != null ? payment.getStatus().name() : "N/A");
            map.put("paymentMethod", payment != null ? payment.getPaymentMethod() : "N/A");
        } catch (Exception e) {
            map.put("paymentStatus", "N/A");
            map.put("paymentMethod", "N/A");
        }

        return map;
    }

    private Map<String, Object> toAdminMap(Order order) {
        Map<String, Object> map = toSafeMap(order);
        try {
            if (order.getUser() != null) {
                map.put("userEmail", order.getUser().getEmail());
                map.put("userName", order.getUser().getName());
            }
        } catch (Exception e) {
            map.put("userEmail", "N/A");
            map.put("userName", "N/A");
        }
        return map;
    }

    @Data
    static class OrderRequest {
        @NotBlank private String address;
        private String paymentMethod;
    }
}
