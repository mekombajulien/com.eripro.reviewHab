package com.eriapro.gestionDesAvis.exception;

public class ValidationCodeExpiredException extends RuntimeException {

    public ValidationCodeExpiredException(String message) {
        super(message);
    }
}
