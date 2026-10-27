package com.vivero.tienda.exception;

// Se lanza cuando algo no cumple las reglas de la tienda, por ejemplo: no hay stock
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
