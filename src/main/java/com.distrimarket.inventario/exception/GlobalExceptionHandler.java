package com.distrimarket.inventario.exception;

import com.distrimarket.commons.dto.ErrorResponseDTO;
import com.distrimarket.commons.dto.FieldErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 400 BAD REQUEST ---
    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Solicitud inválida [400 BAD REQUEST] en {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDTO> details = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            FieldErrorDTO detail = new FieldErrorDTO();
            detail.setField(fieldError.getField());
            detail.setMessage(fieldError.getDefaultMessage());
            details.add(detail);
        }

        log.warn("Fallo de validación en {} {}: {} campo(s) con error. Detalles: {}",
                request.getMethod(), request.getRequestURI(), details.size(), details);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validación de formulario fallida", request, details);
    }

    // --- 401 UNAUTHORIZED ---
    // Atrapa fallos de autenticación cuando se use Spring Security
    /*@ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "No autenticado: se requiere token o credenciales válidas.", request, null);
    }*/

    // --- 403 FORBIDDEN ---
    // Atrapa intentos sin privilegios/roles suficientes
    /*@ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado: permisos insuficientes para ejecutar esta acción.", request, null);
    }*/

    // --- 404 NOT FOUND ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.info("Recurso no encontrado [404 NOT FOUND] en {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    // --- 405 METHOD NOT ALLOWED ---
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedOperation(UnsupportedOperationException ex, HttpServletRequest request) {
        log.warn("Operación no permitida [405 METHOD NOT ALLOWED] en {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, null);
    }

    // --- 409 CONFLICT ---
    // Excepciones de negocio por registros repetidos
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Conflicto de unicidad [409 CONFLICT] en {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    // Violaciones directas a nivel de Base de Datos (ej: unique constraint de CI o RUC)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad referencial o constraint única [409 CONFLICT] en {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT, "El registro entra en conflicto con un valor único ya existente (ej: CI, RUC o código duplicado).", request, null);
    }

    // No se puede incurrir en stock negativo
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
        log.warn("Operación rechazada por stock insuficiente [409 CONFLICT] en {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    // --- 500 INTERNAL SERVER ERROR ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, HttpServletRequest request) {
        // En producción se loguea el error interno (logger.error(ex.getMessage(), ex)) y se oculta el detalle técnico al cliente
        log.error("Error crítico no controlado [500 INTERNAL SERVER ERROR] en {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno inesperado en el servidor.", request, null);
    }

    // --- Método auxiliar para centralizar la construcción del DTO ---
    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String message, HttpServletRequest request, List<FieldErrorDTO> details) {
        ErrorResponseDTO error = new ErrorResponseDTO();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setMessage(message);
        error.setPath(request.getRequestURI());
        if (details != null && !details.isEmpty()) {
            error.setDetails(details);
        }
        return ResponseEntity.status(status).body(error);
    }
}