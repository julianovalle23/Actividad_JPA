package com.vivero.tienda.service;

import com.vivero.tienda.dto.DetallePedidoDTO;

import java.util.List;

/**
 * Consultas sobre las lineas de los pedidos.
 * Los detalles se crean y borran siempre a traves de {@link PedidoService}.
 */
public interface DetallePedidoService {

    DetallePedidoDTO buscarPorId(Long id);

    /** @throws com.vivero.tienda.exception.RecursoNoEncontradoException si el pedido no existe */
    List<DetallePedidoDTO> listarPorPedido(Long pedidoId);
}
