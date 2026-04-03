package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class OtpCooldownException extends AuthException {

    public OtpCooldownException() {
        super(
                "OTP_COOLDOWN",
                "OTP đang trong thời gian chờ, vui lòng thử lại sau",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
