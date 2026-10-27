package com.vivero.tienda.entity;

import com.vivero.tienda.exception.ReglaNegocioException;

/**
 * Cantidad de luz que necesita una planta.
 */
public enum NivelLuz {
    SOL,
    MEDIA,
    SOMBRA;

    /**
     * Convierte un texto (sin importar mayusculas) en un NivelLuz.
     *
     * @throws ReglaNegocioException si el texto no corresponde a ningun nivel
     */
    public static NivelLuz desdeTexto(String texto) {
        for (NivelLuz nivel : values()) {
            if (nivel.name().equalsIgnoreCase(texto)) {
                return nivel;
            }
        }
        throw new ReglaNegocioException("Nivel de luz no valido: " + texto + ". Usa: SOL, MEDIA o SOMBRA");
    }
}
