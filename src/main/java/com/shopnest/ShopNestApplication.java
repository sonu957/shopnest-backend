package com.shopnest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ShopNestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopNestApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  ShopNest E-Commerce Application Started ");
        System.out.println("  API Base URL: http://localhost:8080/api   ");
        System.out.println("===========================================");
    }
}
