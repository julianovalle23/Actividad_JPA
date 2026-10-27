package com.vivero.tienda.service;

import com.vivero.tienda.dto.CategoriaDTO;
import com.vivero.tienda.entity.Categoria;

import java.util.List;

/**
 * Operaciones sobre las categorias de plantas.
 */
public interface CategoriaService {

    /** Lista todas las categorias con su numero de plantas. */
    List<CategoriaDTO> listar();

    CategoriaDTO buscarPorId(Long id);

    /** @throws com.vivero.tienda.exception.ReglaNegocioException si el nombre ya existe */
    CategoriaDTO crear(CategoriaDTO dto);

    /** @throws com.vivero.tienda.exception.ReglaNegocioException si el nombre ya lo usa otra categoria */
    CategoriaDTO actualizar(Long id, CategoriaDTO dto);

    /** @throws com.vivero.tienda.exception.ReglaNegocioException si la categoria tiene plantas */
    void eliminar(Long id);

    /**
     * Devuelve la entidad para que otros servicios la usen dentro de su transaccion.
     *
     * @throws com.vivero.tienda.exception.RecursoNoEncontradoException si no existe
     */
    Categoria obtenerEntidad(Long id);
}
