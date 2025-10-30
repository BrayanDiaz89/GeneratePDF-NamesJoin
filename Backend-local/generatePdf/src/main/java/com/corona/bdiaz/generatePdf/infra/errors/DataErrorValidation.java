package com.corona.bdiaz.generatePdf.infra.errors;

import org.springframework.validation.FieldError;

public record DataErrorValidation(
        String field,
        String error
) {
    public DataErrorValidation(FieldError fieldError){
        this(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
