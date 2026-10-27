package com.vivero.tienda.service;

import com.vivero.tienda.dto.PedidoDTO;

import java.util.List;

/**
 * Operaciones sobre los pedidos. Crear o cancelar un pedido modifica tambien
 * el stock de las plantas, por eso cada operacion es atomica (todo o nada).
 */
public interface PedidoService {

    List<PedidoDTO> listar();

    PedidoDTO buscarPorId(Long id);

    /** @throws com.vivero.tienda.exception.RecursoNoEncontradoException si el cliente no existe */
    List<PedidoDTO> listarPorCliente(Long clienteId);

    /** @param estado PENDIENTE, ENVIADO, ENTREGADO o CANCELADO (sin importar mayusculas) */
    List<PedidoDTO> listarPorEstado(String estado);

    /**
     * Crea un pedido en estado PENDIENTE y descuenta el stock de cada planta.
     * Si alguna linea falla (planta inexistente o sin stock) no se guarda nada.
     */
    PedidoDTO crear(PedidoDTO dto);

    /**
     * Cambia el estado respetando el ciclo de vida de {@link com.vivero.tienda.entity.EstadoPedido}.
     * Al cancelar, el stock vuelve al inventario.
     */
    PedidoDTO cambiarEstado(Long id, String nuevoEstado);

    /** Solo se pueden eliminar pedidos CANCELADOS (su stock ya fue devuelto). */
    void eliminar(Long id);
}
