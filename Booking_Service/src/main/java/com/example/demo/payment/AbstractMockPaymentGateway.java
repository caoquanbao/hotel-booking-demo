package com.example.demo.payment;

import java.math.BigDecimal;
import java.util.Map;

abstract class AbstractMockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentCreateResult createPayment(PaymentCreateCommand command) {
        String gateway = gatewayName().toLowerCase();
        String transactionRef = gateway.toUpperCase() + "-TXN-" + command.paymentOrderId();

        return new PaymentCreateResult(
                gatewayName(),
                command.paymentOrderId(),
                transactionRef,
                "/api/mock-payments/" + gateway + "/" + command.paymentOrderId() + "/pay?result=success",
                gateway + "://mock-pay/" + command.paymentOrderId(),
                "/api/mock-payments/" + gateway + "/" + command.paymentOrderId() + "/qr",
                PaymentStatus.PENDING,
                "Mock payment created successfully"
        );
    }

    @Override
    public PaymentVerificationResult verifyCallback(Map<String, String> payload) {
        String paymentOrderId = extractPaymentOrderId(payload);
        String transactionRef = gatewayName().toUpperCase() + "-TXN-" + paymentOrderId;
        PaymentStatus status = mapResult(payload.get("result"));
        String message = switch (status) {
            case SUCCESS -> "Mock payment marked as success";
            case FAILED -> "Mock payment marked as failed";
            case CANCELLED -> "Mock payment marked as cancelled";
            default -> "Mock payment is pending";
        };

        return new PaymentVerificationResult(
                gatewayName(),
                paymentOrderId,
                transactionRef,
                status,
                message
        );
    }

    @Override
    public PaymentQueryResult queryTransaction(String paymentOrderId) {
        return new PaymentQueryResult(
                gatewayName(),
                paymentOrderId,
                gatewayName().toUpperCase() + "-TXN-" + paymentOrderId,
                PaymentStatus.PENDING,
                "Mock transaction query"
        );
    }

    protected abstract String extractPaymentOrderId(Map<String, String> payload);

    protected BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private PaymentStatus mapResult(String resultRaw) {
        if (resultRaw == null || resultRaw.isBlank()) {
            return PaymentStatus.SUCCESS;
        }
        return switch (resultRaw.trim().toLowerCase()) {
            case "success" -> PaymentStatus.SUCCESS;
            case "failed", "fail", "error" -> PaymentStatus.FAILED;
            case "cancelled", "canceled", "cancel" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }
}
