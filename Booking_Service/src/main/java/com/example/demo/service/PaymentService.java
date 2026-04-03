package com.example.demo.service;

import com.example.demo.client.NotificationClient;
import com.example.demo.dto.BookingConfirmedEvent;
import com.example.demo.dto.NotificationMetadata;
import com.example.demo.dto.NotificationRecipient;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.entity.Booking;
import com.example.demo.payment.PaymentGateway;
import com.example.demo.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    public record PaymentSessionResult(String paymentUrl, String paymentOrderId, String provider) {}

    private final Map<String, PaymentGateway> gateways;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingRepository bookingRepository;
    private final NotificationClient notificationClient;

    public PaymentService(List<PaymentGateway> gateways,
                          ApplicationEventPublisher eventPublisher,
                          BookingRepository bookingRepository,
                          NotificationClient notificationClient) {
        this.gateways = gateways.stream()
                .collect(Collectors.toMap(
                        gateway -> gateway.gatewayName().toUpperCase(Locale.ROOT),
                        Function.identity()
                ));
        this.eventPublisher = eventPublisher;
        this.bookingRepository = bookingRepository;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public PaymentSessionResult createPaymentSession(Booking booking, String paymentMethodRaw) {
        String provider = normalizeProvider(paymentMethodRaw);
        String paymentOrderId = provider + "-" + booking.getId() + "-" + System.currentTimeMillis();
        booking.setPaymentOrderId(paymentOrderId);
        booking.setPaymentProvider(provider);

        PaymentGateway gateway = resolveGateway(provider);
        PaymentGateway.PaymentCreateResult createResult = gateway.createPayment(
                new PaymentGateway.PaymentCreateCommand(
                        paymentOrderId,
                        booking.getId(),
                        booking.getTotalAmount(),
                        "/api/payments/callback",
                        "/api/mock-payments/" + provider.toLowerCase(Locale.ROOT) + "/" + paymentOrderId + "/return"
                )
        );

        log.info("Mock payment created via {} for booking {} with orderId {}",
                createResult.gatewayName(), booking.getId(), createResult.paymentOrderId());

        return new PaymentSessionResult(createResult.payUrl(), paymentOrderId, provider);
    }

    @Transactional
    public boolean handleCallback(Map<String, String> payload) {
        PaymentGateway.PaymentVerificationResult verificationResult = verifyCallback(payload);
        return applyVerificationResult(verificationResult);
    }

    @Transactional
    public PaymentGateway.PaymentVerificationResult handleMockCallback(String gatewayName,
                                                                       String paymentOrderId,
                                                                       String result) {
        String provider = normalizeProvider(gatewayName);
        Map<String, String> payload = new HashMap<>();
        payload.put("gateway", provider);
        payload.put("result", result);
        if ("VNPAY".equals(provider)) {
            payload.put("vnp_TxnRef", paymentOrderId);
        } else {
            payload.put("orderId", paymentOrderId);
        }

        PaymentGateway.PaymentVerificationResult verificationResult = verifyCallback(payload);
        applyVerificationResult(verificationResult);
        return verificationResult;
    }

    private PaymentGateway.PaymentVerificationResult verifyCallback(Map<String, String> payload) {
        String provider = detectProvider(payload);
        PaymentGateway gateway = resolveGateway(provider);
        PaymentGateway.PaymentVerificationResult verificationResult = gateway.verifyCallback(payload);
        log.info("Payment callback verified by {} for order {} with status {}",
                verificationResult.gatewayName(),
                verificationResult.paymentOrderId(),
                verificationResult.status());
        return verificationResult;
    }

    private boolean applyVerificationResult(PaymentGateway.PaymentVerificationResult verificationResult) {
        return switch (verificationResult.status()) {
            case SUCCESS -> {
                markBookingPaid(verificationResult.paymentOrderId());
                yield true;
            }
            case FAILED, CANCELLED, PENDING -> {
                log.info("Payment order {} finished with non-success status {}",
                        verificationResult.paymentOrderId(),
                        verificationResult.status());
                sendPaymentFailedNotificationSafely(verificationResult);
                yield false;
            }
        };
    }

    private void markBookingPaid(String paymentOrderId) {
        Booking booking = bookingRepository.findByPaymentOrderId(paymentOrderId)
                .orElseThrow(() -> new RuntimeException("Booking not found for payment order: " + paymentOrderId));

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            log.info("Skipping duplicate payment confirmation for {}", paymentOrderId);
            return;
        }

        eventPublisher.publishEvent(new BookingConfirmedEvent(paymentOrderId));
    }

    private void sendPaymentFailedNotificationSafely(PaymentGateway.PaymentVerificationResult verificationResult) {
        try {
            Booking booking = bookingRepository.findByPaymentOrderId(verificationResult.paymentOrderId())
                    .orElse(null);

            notificationClient.send(NotificationRequest.builder()
                    .type("PAYMENT_FAILED")
                    .recipient(NotificationRecipient.builder().build())
                    .payload(Map.of(
                            "bookingCode", booking == null ? "UNKNOWN" : String.valueOf(booking.getId()),
                            "paymentCode", verificationResult.paymentOrderId(),
                            "amount", booking == null || booking.getTotalAmount() == null ? "N/A" : booking.getTotalAmount(),
                            "reason", "Payment status: " + verificationResult.status()
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("payment-failed-" + verificationResult.paymentOrderId() + "-" + verificationResult.status())
                            .build())
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to send payment failed notification for order {}",
                    verificationResult.paymentOrderId(), exception);
        }
    }

    private PaymentGateway resolveGateway(String provider) {
        PaymentGateway gateway = gateways.get(provider);
        if (gateway == null) {
            throw new RuntimeException("No payment gateway configured for provider: " + provider);
        }
        return gateway;
    }

    private String detectProvider(Map<String, String> payload) {
        if (payload.containsKey("gateway")) {
            return normalizeProvider(payload.get("gateway"));
        }
        if (payload.containsKey("vnp_TxnRef")) {
            return "VNPAY";
        }
        if (payload.containsKey("orderId")) {
            return "MOMO";
        }
        throw new RuntimeException("Unknown payment callback payload");
    }

    private String normalizeProvider(String paymentMethodRaw) {
        if (paymentMethodRaw == null || paymentMethodRaw.isBlank()) {
            return "MOMO";
        }
        String provider = paymentMethodRaw.trim().toUpperCase(Locale.ROOT);
        if (!"MOMO".equals(provider) && !"VNPAY".equals(provider)) {
            throw new RuntimeException("Unsupported payment method: " + paymentMethodRaw);
        }
        return provider;
    }
}
