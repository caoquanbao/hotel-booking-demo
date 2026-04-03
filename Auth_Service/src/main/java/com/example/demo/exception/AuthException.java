package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public abstract class AuthException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    protected AuthException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
