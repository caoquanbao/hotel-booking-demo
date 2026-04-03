package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends AuthException {

    public TokenExpiredException() {
        super("TOKEN_EXPIRED", "Token đã hết hạn", HttpStatus.BAD_REQUEST);
    }
}
