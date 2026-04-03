package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends AuthException {

    public InvalidOtpException() {
        super(
                "INVALID_OTP",
                "OTP không hợp lệ hoặc đã hết hạn",
                HttpStatus.BAD_REQUEST
        );
    }
}
