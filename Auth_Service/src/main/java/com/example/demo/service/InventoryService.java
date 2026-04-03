package com.example.demo.service;

import com.example.demo.entity.Inventory;
import com.example.demo.entity.Order;
import com.example.demo.repository.InventoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // Dùng cho create order: lock + check + trừ stock
    @Transactional
    public Inventory checkAndDecrease(Long itemId, int quantity) {

        Inventory item = inventoryRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getStock() < quantity) {
            throw new RuntimeException("Out of stock");
        }

        item.decrease(quantity);
        return item; // JPA tự update khi commit
    }

    // Dùng cho scheduler cancel: restore stock
    @Transactional
    public void restoreStock(Order order) {

        // giả sử Order có getItemId() và getQuantity()
        Inventory item = inventoryRepository.findByIdForUpdate(order.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.increase(order.getQuantity());
    }
}