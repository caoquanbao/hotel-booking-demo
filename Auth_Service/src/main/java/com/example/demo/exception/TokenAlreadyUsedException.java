package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class TokenAlreadyUsedException extends AuthException {

    public TokenAlreadyUsedException() {
        super("TOKEN_ALREADY_USED", "Token đã được sử dụng", HttpStatus.BAD_REQUEST);
    }
}
