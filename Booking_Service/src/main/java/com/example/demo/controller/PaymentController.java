package com.example.demo.controller;

import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(value = "/callback", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Map<String, Object>> callback(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam Map<String, String> params) {

        Map<String, String> payload = new HashMap<>();
        params.forEach(payload::put);
        if (body != null) {
            body.forEach((k, v) -> payload.put(k, v == null ? null : String.valueOf(v)));
        }

        boolean success = paymentService.handleCallback(payload);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callbackGet(@RequestParam Map<String, String> params) {
        boolean success = paymentService.handleCallback(params);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }
}
