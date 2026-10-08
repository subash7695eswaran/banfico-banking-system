package com.banfico.banking.exception;

public class ConsentAccessDeniedException extends RuntimeException {
    public ConsentAccessDeniedException(String message) {
        super(message);
    }
}
