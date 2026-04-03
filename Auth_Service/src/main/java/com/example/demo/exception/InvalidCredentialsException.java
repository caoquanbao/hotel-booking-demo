package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AuthException {

    public InvalidCredentialsException() {
        super(
                "INVALID_CREDENTIALS",
                "Email hoặc mật khẩu không hợp lệ",
                HttpStatus.UNAUTHORIZED
        );
    }
}
