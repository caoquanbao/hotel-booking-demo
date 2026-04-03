package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AuthException {

    public EmailAlreadyExistsException() {
        super("EMAIL_ALREADY_EXISTS", "Email đã tồn tại", HttpStatus.CONFLICT);
    }
}
