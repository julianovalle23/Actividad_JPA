package com.vivero.tienda.service;

import com.vivero.tienda.dto.ClienteDTO;
import com.vivero.tienda.entity.Cliente;

import java.util.List;

/**
 * Operaciones sobre los clientes de la tienda.
 */
public interface ClienteService {

    List<ClienteDTO> listar();

    ClienteDTO buscarPorId(Long id);

    /** @throws com.vivero.tienda.exception.ReglaNegocioException si el email ya esta registrado */
    ClienteDTO crear(ClienteDTO dto);

    /** @throws com.vivero.tienda.exception.ReglaNegocioException si el email ya lo usa otro cliente */
    ClienteDTO actualizar(Long id, ClienteDTO dto);

    /** @throws com.vivero.tienda.exception.ReglaNegocioException si el cliente tiene pedidos */
    void eliminar(Long id);

    /**
     * Devuelve la entidad para que otros servicios la usen dentro de su transaccion.
     *
     * @throws com.vivero.tienda.exception.RecursoNoEncontradoException si no existe
     */
    Cliente obtenerEntidad(Long id);
}
