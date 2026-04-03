package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AuthException {

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
