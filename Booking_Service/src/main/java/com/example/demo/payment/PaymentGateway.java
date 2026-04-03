package com.example.demo.payment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGateway {

    String gatewayName();

    PaymentCreateResult createPayment(PaymentCreateCommand command);

    PaymentVerificationResult verifyCallback(Map<String, String> payload);

    PaymentQueryResult queryTransaction(String paymentOrderId);

    record PaymentCreateCommand(
            String paymentOrderId,
            Long bookingId,
            BigDecimal amount,
            String callbackUrl,
            String returnUrl
    ) {}

    record PaymentCreateResult(
            String gatewayName,
            String paymentOrderId,
            String transactionRef,
            String payUrl,
            String deepLink,
            String qrCodeUrl,
            PaymentStatus status,
            String message
    ) {}

    record PaymentVerificationResult(
            String gatewayName,
            String paymentOrderId,
            String transactionRef,
            PaymentStatus status,
            String message
    ) {
        public boolean isSuccess() {
            return status == PaymentStatus.SUCCESS;
        }
    }

    record PaymentQueryResult(
            String gatewayName,
            String paymentOrderId,
            String transactionRef,
            PaymentStatus status,
            String message
    ) {}

    enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED,
        CANCELLED
    }
}
