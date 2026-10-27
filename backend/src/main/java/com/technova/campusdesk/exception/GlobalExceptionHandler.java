package com.technova.campusdesk.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns every error into a uniform ApiError JSON
 * (timestamp, status, error, message, path). Never shows traces or secrets.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** Missing resource -> 404. */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
      HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  /** No permission (own or Spring's) -> 403. */
  @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
  public ResponseEntity<ApiError> handleForbidden(RuntimeException ex, HttpServletRequest request) {
    String message = ex instanceof ForbiddenException ? ex.getMessage()
        : "No tienes permiso para realizar esta operacion";
    return build(HttpStatus.FORBIDDEN, message, request);
  }

  /** Business rule or duplicate -> 409. */
  @ExceptionHandler({BusinessRuleException.class, DuplicateResourceException.class})
  public ResponseEntity<ApiError> handleConflict(RuntimeException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  /**
   * Database constraint or trigger rejected the change -> 409.
   * The SQL message is logged, never returned (it exposes table and column names).
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex,
      HttpServletRequest request) {
    log.warn("Restriccion de base de datos en {}: {}", request.getRequestURI(),
        ex.getMostSpecificCause().getMessage());
    return build(HttpStatus.CONFLICT,
        "La operacion viola una restriccion de integridad de los datos", request);
  }

  /** Invalid business input -> 400. */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex,
      HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  /** Validation errors (@Min, @Max, @Size, @NotBlank, etc.) -> 400 with field details. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    List<String> details = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .toList();
    return build(HttpStatus.BAD_REQUEST, "Los datos enviados no son validos", request, details);
  }

  /** Badly shaped input (malformed JSON, wrong enums or ids...) -> 400. */
  @ExceptionHandler({HttpMessageNotReadableException.class, TypeMismatchException.class})
  public ResponseEntity<ApiError> handleInvalidInput(Exception ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "Los datos enviados no son validos", request);
  }

  /** Wrong email or password -> 401 (generic message, hides which one failed). */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex,
      HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", request);
  }

  /**
   * Any other error -> generic 500 (the cause is logged, not returned).
   * Spring MVC errors that already carry their own status keep it: unknown
   * route (404), wrong HTTP method (405), unsupported media type (415),
   * missing parameter (400)... Otherwise they would turn into a false 500.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
    if (ex instanceof ErrorResponse springError) {
      HttpStatus status = HttpStatus.resolve(springError.getStatusCode().value());
      if (status != null && status.isError()) {
        return build(status, status.getReasonPhrase(), request);
      }
    }
    log.error("Error inesperado en {}", request.getRequestURI(), ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado", request);
  }

  /** Builds the response with status, message and the request path. */
  private ResponseEntity<ApiError> build(HttpStatus status, String message,
      HttpServletRequest request) {
    return build(status, message, request, List.of());
  }

  /** Builds the response with status, message, path and validation details. */
  private ResponseEntity<ApiError> build(HttpStatus status, String message,
      HttpServletRequest request, List<String> details) {
    return ResponseEntity.status(status)
        .body(ApiError.of(status, message, request.getRequestURI(), details));
  }
}
