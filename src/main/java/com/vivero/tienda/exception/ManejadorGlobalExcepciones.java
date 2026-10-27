package com.vivero.tienda.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte las excepciones en respuestas HTTP con el formato {@link ErrorRespuesta}.
 * Asi los controladores no necesitan ningun try/catch.
 */
@Slf4j
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    // ---------- Errores de negocio ----------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                              HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarReglaNegocio(ReglaNegocioException ex,
                                                              HttpServletRequest request) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    // ---------- Errores de validacion ----------

    // Falla alguna anotacion (@NotBlank, @Min, @Email...) de un @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        return crearRespuesta(HttpStatus.BAD_REQUEST, "Hay campos con errores", request, errores);
    }

    // Falla una anotacion en un @RequestParam o @PathVariable (ej: @Min en ?minimo=-1)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacionParametros(HandlerMethodValidationException ex,
                                                                      HttpServletRequest request) {
        StringBuilder mensaje = new StringBuilder();
        for (MessageSourceResolvable error : ex.getAllErrors()) {
            if (mensaje.length() > 0) {
                mensaje.append(". ");
            }
            mensaje.append(error.getDefaultMessage());
        }
        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje.toString(), request, null);
    }

    // JSON mal escrito o un valor que no existe en un enum (ej: "nivelLuz": "LUNA")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonInvalido(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        return crearRespuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no es valido (JSON mal formado o valor no permitido)", request, null);
    }

    // Ej: GET /api/plantas/abc  (se esperaba un numero)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        String mensaje = "El parametro '" + ex.getName() + "' tiene un valor no valido: " + ex.getValue();
        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje, request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                                   HttpServletRequest request) {
        String mensaje = "Falta el parametro obligatorio '" + ex.getParameterName() + "'";
        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje, request, null);
    }

    // ---------- Errores de concurrencia e integridad ----------

    // Otro usuario modifico el mismo registro a la vez (@Version en Planta)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorRespuesta> manejarConcurrencia(ObjectOptimisticLockingFailureException ex,
                                                              HttpServletRequest request) {
        return crearRespuesta(HttpStatus.CONFLICT,
                "El registro fue modificado por otra operacion al mismo tiempo. Intentalo de nuevo", request, null);
    }

    // Ultima barrera: restricciones de la base de datos (unique, foreign key...)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(DataIntegrityViolationException ex,
                                                            HttpServletRequest request) {
        log.warn("Violacion de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return crearRespuesta(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de la base de datos", request, null);
    }

    // ---------- Errores de la peticion HTTP ----------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> manejarRutaInexistente(NoResourceFoundException ex,
                                                                 HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                                   HttpServletRequest request) {
        String mensaje = "El metodo " + ex.getMethod() + " no esta permitido en esta ruta";
        return crearRespuesta(HttpStatus.METHOD_NOT_ALLOWED, mensaje, request, null);
    }

    // Cualquier error no previsto: se registra en el log y no se muestran detalles internos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado en el servidor", request, null);
    }

    private ResponseEntity<ErrorRespuesta> crearRespuesta(HttpStatus status, String mensaje,
                                                          HttpServletRequest request, Map<String, String> errores) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(status.value(), status.getReasonPhrase(),
                mensaje, request.getRequestURI(), errores);
        return new ResponseEntity<>(cuerpo, status);
    }
}
