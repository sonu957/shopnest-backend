//package com.shopnest.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "orders")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Order {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
//    private BigDecimal totalAmount;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private OrderStatus status;
//
//    @Column(columnDefinition = "TEXT")
//    private String address;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//    private List<OrderItem> orderItems;
//
//    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
//    private Payment payment;
//
//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//        if (this.status == null) this.status = OrderStatus.PLACED;
//    }
//
//    public enum OrderStatus {
//        PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
//    }
//}



package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
//@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // EAGER fetch so items always load with order — prevents lazy init exception
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    // EAGER fetch so payment always loads with order
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Payment payment;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.PLACED;
    }

    public enum OrderStatus {
        PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
