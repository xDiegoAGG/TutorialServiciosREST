package com.eafit.tutorial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Respuesta de error estándar de la API")
public class ErrorResponse {

    @Schema(description = "Código de error", example = "PRODUCT_NOT_FOUND")
    private String errorCode;

    @Schema(description = "Mensaje de error", example = "Producto no encontrado con ID: 123")
    private String message;

    @Schema(description = "Detalles adicionales del error")
    private Map<String, String> details;

    @Schema(description = "Timestamp del error", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ErrorResponse() {}

    public ErrorResponse(String errorCode, String message, Map<String, String> details, LocalDateTime timestamp) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, String> getDetails() { return details; }
    public void setDetails(Map<String, String> details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
