package com.vivero.tienda.service;

import com.vivero.tienda.dto.PlantaDTO;
import com.vivero.tienda.entity.Planta;

import java.util.List;

/**
 * Operaciones sobre el catalogo e inventario de plantas.
 */
public interface PlantaService {

    List<PlantaDTO> listar();

    PlantaDTO buscarPorId(Long id);

    /** @throws com.vivero.tienda.exception.RecursoNoEncontradoException si la categoria no existe */
    List<PlantaDTO> buscarPorCategoria(Long categoriaId);

    List<PlantaDTO> buscarPorNombre(String nombre);

    /** @param nivelLuz SOL, MEDIA o SOMBRA (sin importar mayusculas) */
    List<PlantaDTO> buscarPorNivelLuz(String nivelLuz);

    /** Plantas con stock menor o igual a {@code minimo}, de menor a mayor stock. */
    List<PlantaDTO> listarConPocoStock(Integer minimo);

    PlantaDTO crear(PlantaDTO dto);

    PlantaDTO actualizar(Long id, PlantaDTO dto);

    /** @throws com.vivero.tienda.exception.ReglaNegocioException si la planta aparece en algun pedido */
    void eliminar(Long id);

    /**
     * Devuelve la entidad para que otros servicios la usen dentro de su transaccion.
     *
     * @throws com.vivero.tienda.exception.RecursoNoEncontradoException si no existe
     */
    Planta obtenerEntidad(Long id);
}
