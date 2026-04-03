package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long itemId;
    private int quantity;

    private double amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Order() {}

    public Order(Long userId, double amount, OrderStatus status) {
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void transitionTo(OrderStatus newStatus) {
        this.status = newStatus;
    }
}
