package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stock;

    public int getStock() {
        return stock;
    }

    public void decrease(int quantity) {
        this.stock -= quantity;
    }

    public void increase(int quantity) {
        this.stock += quantity;
    }
}