package com.example.demo.entity;

public enum SecurityEventType {

    LOGIN_SUCCESS,
    LOGIN_FAILED,
    OTP_REQUIRED,
    OTP_SUCCESS,
    OTP_FAILED,
    OTP_COOLDOWN,
    PASSWORD_CHANGED,
    LOGOUT,
    ACCOUNT_LOCKED,
    REFRESH_ROTATED
}