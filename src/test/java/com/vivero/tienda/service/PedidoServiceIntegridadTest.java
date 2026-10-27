package com.vivero.tienda.service;

import com.vivero.tienda.dto.DetallePedidoDTO;
import com.vivero.tienda.dto.PedidoDTO;
import com.vivero.tienda.entity.EstadoPedido;
import com.vivero.tienda.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de transacciones e integridad sobre los datos de data.sql.
 * No se usa @Transactional en el test para comprobar el commit/rollback REAL del servicio.
 * Por eso las comprobaciones de stock son relativas (antes vs despues).
 */
@SpringBootTest
class PedidoServiceIntegridadTest {

    private static final Long CLIENTE_MATEO = 2L;
    private static final Long ECHEVERIA = 2L;   // precio 5.00
    private static final Long MONSTERA = 3L;    // precio 25.90
    private static final Long PEDIDO_ENTREGADO = 1L;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PlantaService plantaService;

    @Test
    void crearPedido_descuentaStockYCalculaTotalSinErroresDeRedondeo() {
        int stockAntes = stockDe(ECHEVERIA);

        PedidoDTO creado = pedidoService.crear(pedido(CLIENTE_MATEO, linea(ECHEVERIA, 3)));

        assertEquals(EstadoPedido.PENDIENTE, creado.getEstado());
        assertEquals(0, new BigDecimal("15.00").compareTo(creado.getTotal()));
        assertEquals(stockAntes - 3, stockDe(ECHEVERIA));
    }

    @Test
    void crearPedido_siUnaLineaNoTieneStock_noSeDescuentaNadaRollback() {
        int stockEcheveriaAntes = stockDe(ECHEVERIA);

        // La primera linea es valida, la segunda no: debe deshacerse TODO
        PedidoDTO dto = pedido(CLIENTE_MATEO, linea(ECHEVERIA, 1), linea(MONSTERA, 100));

        assertThrows(ReglaNegocioException.class, () -> pedidoService.crear(dto));
        assertEquals(stockEcheveriaAntes, stockDe(ECHEVERIA));
    }

    @Test
    void cancelarPedido_devuelveElStock() {
        int stockAntes = stockDe(MONSTERA);
        PedidoDTO creado = pedidoService.crear(pedido(CLIENTE_MATEO, linea(MONSTERA, 2)));
        assertEquals(stockAntes - 2, stockDe(MONSTERA));

        pedidoService.cambiarEstado(creado.getId(), "cancelado");

        assertEquals(stockAntes, stockDe(MONSTERA));
    }

    @Test
    void pedidoEntregado_noPuedeCambiarDeEstado() {
        assertThrows(ReglaNegocioException.class,
                () -> pedidoService.cambiarEstado(PEDIDO_ENTREGADO, "PENDIENTE"));
    }

    @Test
    void soloSePuedenEliminarPedidosCancelados() {
        PedidoDTO creado = pedidoService.crear(pedido(CLIENTE_MATEO, linea(ECHEVERIA, 1)));

        assertThrows(ReglaNegocioException.class, () -> pedidoService.eliminar(creado.getId()));

        pedidoService.cambiarEstado(creado.getId(), "CANCELADO");
        pedidoService.eliminar(creado.getId());
    }

    // ---------- metodos de ayuda ----------

    private int stockDe(Long plantaId) {
        return plantaService.buscarPorId(plantaId).getStock();
    }

    private DetallePedidoDTO linea(Long plantaId, int cantidad) {
        DetallePedidoDTO linea = new DetallePedidoDTO();
        linea.setPlantaId(plantaId);
        linea.setCantidad(cantidad);
        return linea;
    }

    private PedidoDTO pedido(Long clienteId, DetallePedidoDTO... lineas) {
        PedidoDTO dto = new PedidoDTO();
        dto.setClienteId(clienteId);
        for (DetallePedidoDTO linea : lineas) {
            dto.getDetalles().add(linea);
        }
        return dto;
    }
}
