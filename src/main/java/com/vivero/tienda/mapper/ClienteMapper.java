package com.vivero.tienda.mapper;

import com.vivero.tienda.dto.ClienteDTO;
import com.vivero.tienda.entity.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        return dto;
    }

    public Cliente toEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        actualizarEntidad(dto, cliente);
        return cliente;
    }

    // Copia los datos editables del DTO a una entidad ya existente
    public void actualizarEntidad(ClienteDTO dto, Cliente cliente) {
        cliente.setNombre(dto.getNombre().trim());
        cliente.setApellido(dto.getApellido().trim());
        cliente.setEmail(dto.getEmail().trim().toLowerCase());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
    }
}
