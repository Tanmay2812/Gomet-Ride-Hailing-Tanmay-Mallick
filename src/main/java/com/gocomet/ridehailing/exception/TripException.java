package com.gocomet.ridehailing.exception;

public class TripException extends RuntimeException {
    public TripException(String message) {
        super(message);
    }
    
    public TripException(String message, Throwable cause) {
        super(message, cause);
    }
}
