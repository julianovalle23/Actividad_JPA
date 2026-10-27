package com.vivero.tienda.controller;

import com.vivero.tienda.dto.DetallePedidoDTO;
import com.vivero.tienda.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Los detalles se crean junto con el pedido, por eso aqui solo hay consultas
@RestController
@RequestMapping("/api/detalles")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedidoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(detallePedidoService.buscarPorId(id));
    }

    // Ejemplo: GET /api/detalles/pedido/1
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<DetallePedidoDTO>> listarPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(detallePedidoService.listarPorPedido(pedidoId));
    }
}
