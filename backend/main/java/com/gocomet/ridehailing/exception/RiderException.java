package com.gocomet.ridehailing.exception;

public class RiderException extends RuntimeException {
    public RiderException(String message) {
        super(message);
    }
    
    public RiderException(String message, Throwable cause) {
        super(message, cause);
    }
}
