package com.vivero.tienda.service.impl;

import com.vivero.tienda.dto.DetallePedidoDTO;
import com.vivero.tienda.entity.DetallePedido;
import com.vivero.tienda.exception.RecursoNoEncontradoException;
import com.vivero.tienda.mapper.DetallePedidoMapper;
import com.vivero.tienda.repository.DetallePedidoRepository;
import com.vivero.tienda.repository.PedidoRepository;
import com.vivero.tienda.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DetallePedidoServiceImpl implements DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoMapper detallePedidoMapper;

    @Override
    public DetallePedidoDTO buscarPorId(Long id) {
        DetallePedido detalle = detallePedidoRepository.findById(id).orElse(null);
        if (detalle == null) {
            throw new RecursoNoEncontradoException("No existe el detalle con id: " + id);
        }
        return detallePedidoMapper.toDTO(detalle);
    }

    @Override
    public List<DetallePedidoDTO> listarPorPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new RecursoNoEncontradoException("No existe el pedido con id: " + pedidoId);
        }

        List<DetallePedidoDTO> resultado = new ArrayList<>();
        for (DetallePedido detalle : detallePedidoRepository.findByPedidoId(pedidoId)) {
            resultado.add(detallePedidoMapper.toDTO(detalle));
        }
        return resultado;
    }
}
