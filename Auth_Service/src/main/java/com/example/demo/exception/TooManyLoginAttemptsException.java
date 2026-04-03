package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class TooManyLoginAttemptsException extends AuthException {

    public TooManyLoginAttemptsException(String message) {
        super(
                "TOO_MANY_LOGIN_ATTEMPTS",
                message,
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
