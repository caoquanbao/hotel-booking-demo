package com.example.demo.dto;

public record ApiSuccessResponse<T>(
        boolean success,
        String message,
        T data
) {
}
