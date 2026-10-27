package com.vivero.tienda.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico para todas las respuestas de error de la API.
 *
 * <pre>
 * {
 *   "fecha": "2026-10-27T16:31:48",
 *   "estado": 404,
 *   "error": "Not Found",
 *   "mensaje": "No existe el cliente con id: 99",
 *   "ruta": "/api/clientes/99"
 * }
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorRespuesta {

    private final LocalDateTime fecha;
    private final int estado;
    private final String error;
    private final String mensaje;
    private final String ruta;

    // Solo aparece en errores de validacion: campo -> mensaje
    private final Map<String, String> errores;

    public ErrorRespuesta(int estado, String error, String mensaje, String ruta, Map<String, String> errores) {
        this.fecha = LocalDateTime.now();
        this.estado = estado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.errores = errores;
    }
}
