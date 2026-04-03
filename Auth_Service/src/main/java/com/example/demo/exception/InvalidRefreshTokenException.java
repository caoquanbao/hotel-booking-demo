package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends AuthException {

    public InvalidRefreshTokenException() {
        super(
                "INVALID_REFRESH_TOKEN",
                "Refresh token không hợp lệ",
                HttpStatus.UNAUTHORIZED
        );
    }
}
