package com.example.demo.exception;

public class AdsAuctionException extends RuntimeException {
    public AdsAuctionException(String message) {
        super(message);
    }

    public AdsAuctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
