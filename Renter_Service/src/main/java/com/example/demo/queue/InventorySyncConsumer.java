package com.example.demo.queue;

import com.example.demo.dto.InventorySyncRequest;
import com.example.demo.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySyncConsumer {

    private final InventoryService inventoryService;

    @RabbitListener(queues = "inventory.sync.queue")
    public void consume(InventorySyncRequest request) {
        log.info("Received inventory sync requestId={}", request.getRequestId());
        inventoryService.processInventorySync(request);
    }
}
