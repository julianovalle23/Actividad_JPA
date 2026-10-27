package com.vivero.tienda.exception;

// Se lanza cuando no existe un registro con el id buscado
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
