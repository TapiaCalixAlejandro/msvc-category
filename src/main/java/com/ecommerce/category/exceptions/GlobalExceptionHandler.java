package com.ecommerce.category.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //  Cuando no se encuentra un recurso 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    //  Maneja errores al subir o actualizar un archivo
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(FileStorageException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error de almacenamiento de archivo.",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    //  Cuando la solicitud no es valida 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //  Maneja errores de validación @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> HandleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        //  Obtener los errores de campo especifico
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        //  Armar la respuesta completa
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation error");
        body.put("message", "Existen errores de validación en la solicitud.");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", LocalDateTime.now());
        body.put("fields", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    //  Excepciones genericas no controladas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> hanldeGlobalException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    //
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error al acceder a los datos",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    //
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinnesException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de negocio.",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
