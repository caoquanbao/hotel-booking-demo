package com.example.demo.payment;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VnPayMockPaymentGateway extends AbstractMockPaymentGateway {

    @Override
    public String gatewayName() {
        return "VNPAY";
    }

    @Override
    protected String extractPaymentOrderId(Map<String, String> payload) {
        return payload.getOrDefault("vnp_TxnRef", payload.get("paymentOrderId"));
    }
}
