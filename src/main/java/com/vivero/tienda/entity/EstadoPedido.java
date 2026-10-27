package com.vivero.tienda.entity;

import com.vivero.tienda.exception.ReglaNegocioException;

/**
 * Ciclo de vida de un pedido.
 *
 * <pre>
 * PENDIENTE --> ENVIADO --> ENTREGADO
 *     |            |
 *     +------------+--> CANCELADO
 * </pre>
 *
 * ENTREGADO y CANCELADO son estados finales: ya no pueden cambiar.
 */
public enum EstadoPedido {
    PENDIENTE,
    ENVIADO,
    ENTREGADO,
    CANCELADO;

    /**
     * Indica si se permite pasar de este estado al estado {@code nuevo}.
     */
    public boolean puedeCambiarA(EstadoPedido nuevo) {
        if (this == PENDIENTE) {
            return nuevo == ENVIADO || nuevo == CANCELADO;
        }
        if (this == ENVIADO) {
            return nuevo == ENTREGADO || nuevo == CANCELADO;
        }
        return false;
    }

    /**
     * Convierte un texto (sin importar mayusculas) en un EstadoPedido.
     *
     * @throws ReglaNegocioException si el texto no corresponde a ningun estado
     */
    public static EstadoPedido desdeTexto(String texto) {
        for (EstadoPedido estado : values()) {
            if (estado.name().equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        throw new ReglaNegocioException("Estado no valido: " + texto
                + ". Usa: PENDIENTE, ENVIADO, ENTREGADO o CANCELADO");
    }
}
