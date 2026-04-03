package com.example.demo.exception;

import com.example.demo.client.NotificationClient;
import com.example.demo.dto.ApiErrorResponse;
import com.example.demo.dto.NotificationMetadata;
import com.example.demo.dto.NotificationRecipient;
import com.example.demo.dto.NotificationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final NotificationClient notificationClient;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Invalid request");

        logError(ex, request, message);
        return errorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(ValidationException ex,
                                                             HttpServletRequest request) {
        logError(ex, request, ex.getMessage());
        return errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternal(ExternalServiceException ex,
                                                           HttpServletRequest request) {
        logError(ex, request, ex.getMessage());
        return errorResponse(HttpStatus.BAD_GATEWAY, "BANK_API_FAILED", ex.getMessage());
    }

    @ExceptionHandler(InventoryValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleInventoryValidation(InventoryValidationException ex,
                                                                      HttpServletRequest request) {
        logError(ex, request, ex.getMessage());
        return errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "INVENTORY_VALIDATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(InventoryBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleInventoryBusiness(InventoryBusinessException ex,
                                                                    HttpServletRequest request) {
        logError(ex, request, ex.getMessage());
        return errorResponse(HttpStatus.CONFLICT, "INVENTORY_BUSINESS_RULE_FAILED", ex.getMessage());
    }

    @ExceptionHandler(AdsAuctionException.class)
    public ResponseEntity<ApiErrorResponse> handleAds(AdsAuctionException ex,
                                                      HttpServletRequest request) {
        logError(ex, request, ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "ADS_AUCTION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(WalletOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleWallet(WalletOperationException ex,
                                                         HttpServletRequest request) {
        logError(ex, request, ex.getMessage());
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "WALLET_OPERATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOthers(Exception ex,
                                                         HttpServletRequest request) {
        logError(ex, request, ex.getMessage());
        sendSystemErrorNotificationSafely(ex, request);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
    }

    private void logError(Exception ex, HttpServletRequest request, String message) {
        log.error("ERROR at {} {}: {}", request.getMethod(), request.getRequestURI(), message, ex);
    }

    private void sendSystemErrorNotificationSafely(Exception ex, HttpServletRequest request) {
        try {
            notificationClient.send(NotificationRequest.builder()
                    .type("SYSTEM_ERROR")
                    .recipient(NotificationRecipient.builder().build())
                    .payload(Map.of(
                            "serviceName", "Renter_Service",
                            "errorCode", "INTERNAL_ERROR",
                            "errorMessage", ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                            "occurredAt", Instant.now().toString()
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("renter-system-error-" + request.getMethod() + "-" + request.getRequestURI() + "-" + Instant.now().toEpochMilli())
                            .build())
                    .build());
        } catch (Exception notifyException) {
            log.warn("Failed to send system error notification for renter service", notifyException);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status,
                                                           String code,
                                                           String message) {
        return ResponseEntity.status(status).body(ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build());
    }
}
