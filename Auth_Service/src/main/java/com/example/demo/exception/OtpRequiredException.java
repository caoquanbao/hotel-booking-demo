package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class OtpRequiredException extends AuthException {

    public OtpRequiredException() {
        super(
                "OTP_REQUIRED",
                "Tài khoản đang yêu cầu xác thực OTP",
                HttpStatus.FORBIDDEN
        );
    }
}
