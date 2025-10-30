package com.corona.bdiaz.generatePdf.infra.errors;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
