package com.example.demo.queue;

import com.example.demo.dto.InventorySyncRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventorySyncProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(InventorySyncRequest request) {
        rabbitTemplate.convertAndSend(
                "inventory.exchange",
                "inventory.sync",
                request
        );
    }
}
