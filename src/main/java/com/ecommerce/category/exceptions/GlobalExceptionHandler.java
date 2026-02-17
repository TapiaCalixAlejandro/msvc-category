package com.ecommerce.category.exceptions;

import com.ecommerce.category.models.dtos.ApiResponse;
import com.ecommerce.category.models.dtos.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.*;

/**
 * Manejo global de excepciones REST.
 * Encapsula las respuestas en ApiResponse y captura información util (traceId).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ApiResponse build(HttpStatus status, String error, String message, HttpServletRequest path, List<ValidationError> errors) {
        ApiResponse api = new ApiResponse();
        api.setTimestamp(Instant.now());
        api.setStatus(status.value());
        api.setError(status.getReasonPhrase());
        api.setMessage(message);
        api.setPath(path.getRequestURI());
        api.setValidationErrors(errors);
        return api;
    }

    private ApiResponse build(HttpStatus status, String message, HttpServletRequest path, List<ValidationError> errors) {
        ApiResponse api = new ApiResponse();
        api.setTimestamp(Instant.now());
        api.setStatus(status.value());
        api.setError(status.getReasonPhrase());
        api.setMessage(message);
        api.setPath(path.getRequestURI());
        api.setValidationErrors(errors);
        return api;
    }

    // 400 - Errores de validación en @RequestPart (Ej: JSON mezclado con Archivos/Imágenes en multipart)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest req) {
        List<ValidationError> errors = ex.getBeanResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream().map(err -> {
                    String paramName = result.getMethodParameter().getParameterName();
                    String fieldName = (err instanceof FieldError fe) ? paramName + "." + fe.getField() : paramName;
                    Object rejectedValue = (err instanceof FieldError fe) ? fe.getRejectedValue() : null;
                    return new ValidationError(fieldName, err.getDefaultMessage(), rejectedValue);
                }))
                .toList();
        log.warn("Validación de método fallida (Multipart/RequestPart): {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, ex.getMessage(), "Error en los parametros enviados", req, errors));
    }

    // 400 - Bean Validation errors from @Valid (JSON @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleArgumentNotValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .toList();
        log.warn("Validación fallida en request (JSON): {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, "Validación fallida", "Errores en los datos del cuerpo (JSON)", request, errors));
    }

    // 400 - Errores en parámetros de URL o Path (Ej: @RequestParam o @PathVariable que fallan validaciones directas)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String rawPath = cv.getPropertyPath().toString();
                    String fieldName = rawPath.contains(".") ? rawPath.substring(rawPath.lastIndexOf(".") + 1) : rawPath;
                    return new ValidationError(fieldName, cv.getMessage(), cv.getInvalidValue());
                })
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, ex.getMessage(), "Error en parámetros de consulta o ruta", request, errors));
    }

    // 400 - JSON mal formado (Ej: Falta una coma, un corchete, o el formato de fecha es irreconocible)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Cuerpo de la petición ilegible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, ex.getMessage(), "El formato del cuerpo de la petición es incorrecto o está mal formado", request, null));
    }

    // 400 - Error de conversión de tipos (Ej: Se esperaba un número ID en la URL y se envió una palabra/texto)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String detail = String.format("El parámetro '%s' con valor '%s' no se pudo convertir al tipo requerido (%s)",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        log.warn("Error de tipo de dato: {}", detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, ex.getMessage(), detail, request, null));
    }

    //  404 - Recurso no encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Categoría no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null));
    }

    // 409 - Conflict
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinnes(Exception ex, HttpServletRequest request) {
        log.warn("Conflicto de negocio categorias: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), request, null));
    }

    // 422 - Validation Business (opcional: mapear a 422 o 400 según política)
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<ApiResponse> handleValidate(ValidateException ex, HttpServletRequest request) {
        List<ValidationError> errors = Optional.ofNullable(ex.getErrors())
                .orElse(Collections.emptyList())
                .stream()
                .map(e -> new ValidationError("negocio", e, null))
                .toList();
        log.warn("Regla de negocio violada: {}", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(build(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio", "No se pudo procesar la solicitud debido a restricciones del sistema", request, errors));
    }

    // 500 - Error al guardar archivos (Imágenes de categorías)
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse> handleFileStorage(FileStorageException ex, HttpServletRequest request) {
        log.error("Error de archivos: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "Error de Almacenamiento", request, null));
    }

    // 500 - Error de Base de Datos
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiResponse> handleDatabase(DatabaseException ex, HttpServletRequest request) {
        log.error("Error de persistencia: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(HttpStatus.INTERNAL_SERVER_ERROR, "Error de Datos", "Ocurrió un error inesperado en la base de datos", request, null));
    }

    // 500 - El 'Atrapa-todo' (Cualquier cosa no prevista)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleAll(Exception ex, HttpServletRequest request) {
        log.error("ERROR NO CONTROLADO: ", ex); // Aquí logueamos todo el StackTrace para debug
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno", "Ha ocurrido un error inesperado en el servidor", request, null));
    }
}
