//package com.shopnest.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "users")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 100)
//    private String name;
//
//    @Column(nullable = false, unique = true, length = 100)
//    private String email;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Role role;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Order> orders;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<CartItem> cartItems;
//
//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//    }
//
//    public enum Role {
//        CUSTOMER, ADMIN
//    }
//}



package com.shopnest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore  // Never serialize password
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonIgnore  // Prevent circular: User -> Orders -> User -> Orders...
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

    @JsonIgnore  // Prevent circular: User -> CartItems -> User...
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Role {
        CUSTOMER, ADMIN
    }
}