package com.eafit.tutorial.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.eafit.tutorial.dto.ApiResponse;
import com.eafit.tutorial.dto.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        logger.warn("Error de validación: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Error de validación en los datos enviados",
            errors,
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorResponse, "Datos de entrada inválidos", 400));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolation(
            ConstraintViolationException ex) {

        logger.warn("Error de validación de parámetros: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            "PARAMETER_VALIDATION_ERROR",
            "Error de validación en parámetros",
            errors,
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorResponse, "Parámetros inválidos", 400));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleProductNotFound(
            ProductNotFoundException ex, WebRequest request) {

        logger.warn("Producto no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "PRODUCT_NOT_FOUND",
            ex.getMessage(),
            Map.of("path", request.getDescription(false).replace("uri=", "")),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(errorResponse, "Recurso no encontrado", 404));
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleProductAlreadyExists(
            ProductAlreadyExistsException ex) {

        logger.warn("Producto ya existe: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "PRODUCT_ALREADY_EXISTS",
            ex.getMessage(),
            Map.of("suggestion", "Use un nombre diferente o actualice el producto existente"),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(errorResponse, "Conflicto de recursos", 409));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
            ValidationException ex) {

        logger.warn("Error de validación personalizado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "CUSTOM_VALIDATION_ERROR",
            ex.getMessage(),
            ex.getErrors() != null ? ex.getErrors() : Map.of(),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorResponse, "Error de validación", 400));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        logger.warn("Error de tipo de dato: {}", ex.getMessage());

        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        String message = String.format("El parámetro '%s' debe ser de tipo %s",
            ex.getName(),
            typeName);

        ErrorResponse errorResponse = new ErrorResponse(
            "TYPE_MISMATCH_ERROR",
            message,
            Map.of("parameter", ex.getName(), "expectedType", typeName),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorResponse, "Tipo de dato incorrecto", 400));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        logger.warn("Error de formato JSON: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "MALFORMED_JSON",
            "El formato JSON enviado es inválido",
            Map.of("suggestion", "Verifique la sintaxis JSON del cuerpo de la petición"),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorResponse, "Formato JSON inválido", 400));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex) {

        logger.warn("Argumento ilegal: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "ILLEGAL_ARGUMENT",
            ex.getMessage(),
            Map.of("suggestion", "Verifique los valores enviados en la petición"),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorResponse, "Argumento inválido", 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGlobalException(
            Exception ex, WebRequest request) {

        logger.error("Error interno no controlado: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "Ha ocurrido un error interno en el servidor",
            Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "path", request.getDescription(false).replace("uri=", "")
            ),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(errorResponse, "Error interno del servidor", 500));
    }
}
