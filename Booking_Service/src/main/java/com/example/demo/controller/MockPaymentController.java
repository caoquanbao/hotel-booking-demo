package com.example.demo.controller;

import com.example.demo.payment.PaymentGateway;
import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mock-payments")
public class MockPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{gateway}/{paymentOrderId}/pay")
    public ResponseEntity<Map<String, Object>> pay(@PathVariable String gateway,
                                                   @PathVariable String paymentOrderId,
                                                   @RequestParam(defaultValue = "success") String result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("gatewayName", gateway.toUpperCase());
        response.put("orderId", paymentOrderId);
        response.put("status", PaymentGateway.PaymentStatus.PENDING.name());
        response.put("message", "Mock payment page. Call the callback URL to simulate gateway notification.");
        response.put("callbackUrl", "/api/mock-payments/" + gateway + "/" + paymentOrderId + "/callback?result=" + result);
        response.put("returnUrl", "/api/mock-payments/" + gateway + "/" + paymentOrderId + "/return?result=" + result);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{gateway}/{paymentOrderId}/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> callback(@PathVariable String gateway,
                                                        @PathVariable String paymentOrderId,
                                                        @RequestParam(defaultValue = "success") String result) {
        PaymentGateway.PaymentVerificationResult verificationResult =
                paymentService.handleMockCallback(gateway, paymentOrderId, result);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("gatewayName", verificationResult.gatewayName());
        response.put("orderId", verificationResult.paymentOrderId());
        response.put("transactionRef", verificationResult.transactionRef());
        response.put("status", verificationResult.status().name());
        response.put("success", verificationResult.isSuccess());
        response.put("message", verificationResult.message());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gateway}/{paymentOrderId}/return")
    public ResponseEntity<Map<String, Object>> returnUrl(@PathVariable String gateway,
                                                         @PathVariable String paymentOrderId,
                                                         @RequestParam(defaultValue = "success") String result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("gatewayName", gateway.toUpperCase());
        response.put("orderId", paymentOrderId);
        response.put("status", result.toUpperCase());
        response.put("message", "Mock payment return URL");
        return ResponseEntity.ok(response);
    }
}
