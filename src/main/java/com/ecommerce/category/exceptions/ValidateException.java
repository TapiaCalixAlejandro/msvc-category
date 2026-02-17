package com.ecommerce.category.exceptions;

import java.util.List;

public class ValidateException extends RuntimeException {
    private final List<String> errors;

    // Constructor para un solo mensaje
    public ValidateException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    // Constructor para una lista de mensajes (el que usas en el stream)
    public ValidateException(List<String> errors) {
        super("Múltiples errores de validación de negocio");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
