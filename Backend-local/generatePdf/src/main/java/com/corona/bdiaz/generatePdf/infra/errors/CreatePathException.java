package com.corona.bdiaz.generatePdf.infra.errors;

public class CreatePathException extends RuntimeException {
    public CreatePathException(String message) {
        super(message);
    }
}
