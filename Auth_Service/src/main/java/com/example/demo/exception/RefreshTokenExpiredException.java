package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredException extends AuthException {

    public RefreshTokenExpiredException() {
        super(
                "REFRESH_EXPIRED",
                "Refresh token đã hết hạn",
                HttpStatus.UNAUTHORIZED
        );
    }
}
