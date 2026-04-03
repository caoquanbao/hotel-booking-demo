package com.example.demo.dto;

public class PaymentCallback {

    private Long orderId;
    private String status; // "SUCCESS" / "FAILED" (mock)

    public PaymentCallback() {}

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}