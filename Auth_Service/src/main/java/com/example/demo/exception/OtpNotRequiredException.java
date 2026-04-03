package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class OtpNotRequiredException extends AuthException {

    public OtpNotRequiredException() {
        super(
                "OTP_NOT_REQUIRED",
                "Tài khoản hiện không yêu cầu OTP",
                HttpStatus.BAD_REQUEST
        );
    }
}
