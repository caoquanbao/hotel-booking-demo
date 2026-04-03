package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends AuthException {

    public EmailNotVerifiedException() {
        super(
                "EMAIL_NOT_VERIFIED",
                "Tài khoản chưa xác minh email",
                HttpStatus.FORBIDDEN
        );
    }
}
