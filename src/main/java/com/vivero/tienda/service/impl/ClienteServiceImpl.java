package com.vivero.tienda.service.impl;

import com.vivero.tienda.dto.ClienteDTO;
import com.vivero.tienda.entity.Cliente;
import com.vivero.tienda.exception.RecursoNoEncontradoException;
import com.vivero.tienda.exception.ReglaNegocioException;
import com.vivero.tienda.mapper.ClienteMapper;
import com.vivero.tienda.repository.ClienteRepository;
import com.vivero.tienda.repository.PedidoRepository;
import com.vivero.tienda.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public List<ClienteDTO> listar() {
        List<ClienteDTO> resultado = new ArrayList<>();
        for (Cliente cliente : clienteRepository.findAll()) {
            resultado.add(clienteMapper.toDTO(cliente));
        }
        return resultado;
    }

    @Override
    public ClienteDTO buscarPorId(Long id) {
        return clienteMapper.toDTO(obtenerEntidad(id));
    }

    @Override
    @Transactional
    public ClienteDTO crear(ClienteDTO dto) {
        if (clienteRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            throw new ReglaNegocioException("Ya existe un cliente con el email: " + dto.getEmail());
        }
        Cliente guardado = clienteRepository.save(clienteMapper.toEntity(dto));
        return clienteMapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public ClienteDTO actualizar(Long id, ClienteDTO dto) {
        Cliente cliente = obtenerEntidad(id);
        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail().trim(), id)) {
            throw new ReglaNegocioException("El email " + dto.getEmail() + " ya lo usa otro cliente");
        }
        clienteMapper.actualizarEntidad(dto, cliente);
        return clienteMapper.toDTO(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = obtenerEntidad(id);
        if (pedidoRepository.existsByClienteId(id)) {
            throw new ReglaNegocioException("No se puede eliminar un cliente que tiene pedidos");
        }
        clienteRepository.delete(cliente);
    }

    @Override
    public Cliente obtenerEntidad(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            throw new RecursoNoEncontradoException("No existe el cliente con id: " + id);
        }
        return cliente;
    }
}
