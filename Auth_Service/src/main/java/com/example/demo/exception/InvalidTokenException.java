package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException() {
        super("INVALID_TOKEN", "Token không hợp lệ", HttpStatus.BAD_REQUEST);
    }
}
