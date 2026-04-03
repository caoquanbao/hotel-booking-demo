package com.example.demo.controller;

import com.example.demo.dto.ApiErrorResponse;
import com.example.demo.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(AuthException ex,
                                                                HttpServletRequest request) {
        log.error("ERROR tại {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus())
                .body(buildBody(ex.getCode(), ex.getMessage(), request));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex,
                                                                   HttpServletRequest request) {
        log.error("ERROR tại {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody("INTERNAL_SERVER_ERROR", ex.getMessage(), request));
    }

    private ApiErrorResponse buildBody(String code, String message, HttpServletRequest request) {
        return new ApiErrorResponse(
                code,
                message,
                LocalDateTime.now(),
                request.getRequestURI()
        );
    }
}
