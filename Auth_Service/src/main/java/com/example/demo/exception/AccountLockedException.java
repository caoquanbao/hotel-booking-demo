package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends AuthException {

    public AccountLockedException() {
        super(
                "ACCOUNT_LOCKED",
                "Tài khoản đang bị khóa tạm thời",
                HttpStatus.LOCKED
        );
    }
}
