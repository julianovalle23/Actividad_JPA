package com.vivero.tienda.mapper;

import com.vivero.tienda.dto.DetallePedidoDTO;
import com.vivero.tienda.dto.PedidoDTO;
import com.vivero.tienda.entity.DetallePedido;
import com.vivero.tienda.entity.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PedidoMapper {

    // Reutilizamos el mapper de detalles
    private final DetallePedidoMapper detallePedidoMapper;

    /**
     * Requiere que el cliente, los detalles y sus plantas ya esten cargados
     * (los metodos de PedidoRepository los traen con @EntityGraph).
     */
    public PedidoDTO toDTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setId(pedido.getId());
        dto.setClienteId(pedido.getCliente().getId());
        dto.setClienteNombre(pedido.getCliente().getNombreCompleto());
        dto.setFecha(pedido.getFecha());
        dto.setEstado(pedido.getEstado());
        dto.setTotal(pedido.getTotal());

        List<DetallePedidoDTO> detallesDTO = new ArrayList<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            detallesDTO.add(detallePedidoMapper.toDTO(detalle));
        }
        dto.setDetalles(detallesDTO);
        return dto;
    }
}
