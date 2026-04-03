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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final NotificationClient notificationClient;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Invalid request");
        return errorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request);
    }

    @ExceptionHandler(ReviewValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleReviewValidation(ReviewValidationException ex,
                                                                   HttpServletRequest request) {
        return errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "REVIEW_VALIDATION_FAILED", ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                           HttpServletRequest request) {
        return errorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex,
                                                                 HttpServletRequest request) {
        return errorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                               HttpServletRequest request) {
        return errorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request);
    }

    @ExceptionHandler({IllegalStateException.class, BookingConflictException.class, InsufficientInventoryException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return errorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOthers(Exception ex, HttpServletRequest request) {
        log.error("ERROR at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        sendSystemErrorNotificationSafely(ex, request);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), request);
    }

    private void sendSystemErrorNotificationSafely(Exception ex, HttpServletRequest request) {
        try {
            notificationClient.send(NotificationRequest.builder()
                    .type("SYSTEM_ERROR")
                    .recipient(NotificationRecipient.builder().build())
                    .payload(Map.of(
                            "serviceName", "Booking_Service",
                            "errorCode", "INTERNAL_ERROR",
                            "errorMessage", ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                            "occurredAt", Instant.now().toString()
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("booking-system-error-" + request.getMethod() + "-" + request.getRequestURI() + "-" + Instant.now().toEpochMilli())
                            .build())
                    .build());
        } catch (Exception notifyException) {
            log.warn("Failed to send system error notification for booking service", notifyException);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status,
                                                           String error,
                                                           String message,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        ));
    }
}
