package com.example.demo.payment;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MomoMockPaymentGateway extends AbstractMockPaymentGateway {

    @Override
    public String gatewayName() {
        return "MOMO";
    }

    @Override
    protected String extractPaymentOrderId(Map<String, String> payload) {
        return payload.getOrDefault("orderId", payload.get("paymentOrderId"));
    }
}
