package com.example.demo.exception;

public class InventoryBusinessException extends RuntimeException {
    public InventoryBusinessException(String message) {
        super(message);
    }
}
