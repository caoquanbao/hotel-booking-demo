package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange inventoryExchange() {
        return new DirectExchange("inventory.exchange", true, false);
    }

    @Bean
    public Queue inventorySyncQueue() {
        return QueueBuilder.durable("inventory.sync.queue").build();
    }

    @Bean
    public Binding inventorySyncBinding(DirectExchange inventoryExchange, Queue inventorySyncQueue) {
        return BindingBuilder.bind(inventorySyncQueue)
                .to(inventoryExchange)
                .with("inventory.sync");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
