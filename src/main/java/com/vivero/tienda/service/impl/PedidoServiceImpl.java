package com.vivero.tienda.service.impl;

import com.vivero.tienda.dto.DetallePedidoDTO;
import com.vivero.tienda.dto.PedidoDTO;
import com.vivero.tienda.entity.Cliente;
import com.vivero.tienda.entity.DetallePedido;
import com.vivero.tienda.entity.EstadoPedido;
import com.vivero.tienda.entity.Pedido;
import com.vivero.tienda.entity.Planta;
import com.vivero.tienda.exception.RecursoNoEncontradoException;
import com.vivero.tienda.exception.ReglaNegocioException;
import com.vivero.tienda.mapper.PedidoMapper;
import com.vivero.tienda.repository.PedidoRepository;
import com.vivero.tienda.service.ClienteService;
import com.vivero.tienda.service.PedidoService;
import com.vivero.tienda.service.PlantaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Logica de pedidos.
 *
 * <p><b>Estrategia transaccional:</b></p>
 * <ul>
 *   <li>Las consultas usan {@code readOnly = true} (valor por defecto de la clase).</li>
 *   <li>Crear y cambiar estado tocan dos tablas (pedidos y plantas). Al ser {@code @Transactional},
 *       si se lanza cualquier RuntimeException (por ejemplo {@link ReglaNegocioException} por falta
 *       de stock) se hace rollback y ningun stock queda descontado a medias.</li>
 *   <li>{@code Planta} tiene {@code @Version}: si dos pedidos compran la misma planta a la vez,
 *       el segundo falla con un conflicto en vez de dejar el stock mal calculado.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteService clienteService;
    private final PlantaService plantaService;
    private final PedidoMapper pedidoMapper;

    @Override
    public List<PedidoDTO> listar() {
        return convertirLista(pedidoRepository.findAll());
    }

    @Override
    public PedidoDTO buscarPorId(Long id) {
        return pedidoMapper.toDTO(obtenerPedido(id));
    }

    @Override
    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        clienteService.obtenerEntidad(clienteId); // 404 si el cliente no existe
        return convertirLista(pedidoRepository.findByClienteIdOrderByFechaDesc(clienteId));
    }

    @Override
    public List<PedidoDTO> listarPorEstado(String estado) {
        return convertirLista(pedidoRepository.findByEstado(EstadoPedido.desdeTexto(estado)));
    }

    @Override
    @Transactional
    public PedidoDTO crear(PedidoDTO dto) {
        Cliente cliente = clienteService.obtenerEntidad(dto.getClienteId());

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFecha(LocalDate.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        for (DetallePedidoDTO linea : dto.getDetalles()) {
            pedido.agregarDetalle(crearDetalle(linea));
        }

        // CascadeType.ALL guarda tambien los detalles
        return pedidoMapper.toDTO(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoDTO cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = obtenerPedido(id);
        EstadoPedido estadoNuevo = EstadoPedido.desdeTexto(nuevoEstado);

        validarCambioEstado(pedido.getEstado(), estadoNuevo);

        if (estadoNuevo == EstadoPedido.CANCELADO) {
            devolverStock(pedido);
        }
        pedido.setEstado(estadoNuevo);

        // Sin save(): Hibernate detecta los cambios (dirty checking) y los guarda al hacer commit
        return pedidoMapper.toDTO(pedido);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Pedido pedido = obtenerPedido(id);
        if (pedido.getEstado() != EstadoPedido.CANCELADO) {
            throw new ReglaNegocioException("Solo se pueden eliminar pedidos cancelados. "
                    + "Estado actual: " + pedido.getEstado());
        }
        pedidoRepository.delete(pedido);
    }

    /**
     * Crea una linea del pedido: comprueba el stock y lo descuenta.
     */
    private DetallePedido crearDetalle(DetallePedidoDTO linea) {
        Planta planta = plantaService.obtenerEntidad(linea.getPlantaId());
        int cantidad = linea.getCantidad();

        if (!planta.tieneStock(cantidad)) {
            throw new ReglaNegocioException("No hay suficiente stock de " + planta.getNombre()
                    + ". Disponible: " + planta.getStock() + ", solicitado: " + cantidad);
        }
        planta.descontarStock(cantidad);
        return new DetallePedido(planta, cantidad);
    }

    private void validarCambioEstado(EstadoPedido actual, EstadoPedido nuevo) {
        if (!actual.puedeCambiarA(nuevo)) {
            throw new ReglaNegocioException("No se puede pasar un pedido de " + actual + " a " + nuevo);
        }
    }

    private void devolverStock(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            detalle.getPlanta().reponerStock(detalle.getCantidad());
        }
    }

    private Pedido obtenerPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id).orElse(null);
        if (pedido == null) {
            throw new RecursoNoEncontradoException("No existe el pedido con id: " + id);
        }
        return pedido;
    }

    private List<PedidoDTO> convertirLista(List<Pedido> pedidos) {
        List<PedidoDTO> resultado = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            resultado.add(pedidoMapper.toDTO(pedido));
        }
        return resultado;
    }
}
