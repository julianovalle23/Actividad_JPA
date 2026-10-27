package com.vivero.tienda.mapper;

import com.vivero.tienda.dto.DetallePedidoDTO;
import com.vivero.tienda.entity.DetallePedido;
import org.springframework.stereotype.Component;

@Component
public class DetallePedidoMapper {

    public DetallePedidoDTO toDTO(DetallePedido detalle) {
        DetallePedidoDTO dto = new DetallePedidoDTO();
        dto.setId(detalle.getId());
        dto.setPlantaId(detalle.getPlanta().getId());
        dto.setPlantaNombre(detalle.getPlanta().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
