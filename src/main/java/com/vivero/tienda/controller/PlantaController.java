package com.vivero.tienda.controller;

import com.vivero.tienda.dto.PlantaDTO;
import com.vivero.tienda.service.PlantaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantas")
@RequiredArgsConstructor
public class PlantaController {

    private final PlantaService plantaService;

    @GetMapping
    public ResponseEntity<List<PlantaDTO>> listar() {
        return ResponseEntity.ok(plantaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(plantaService.buscarPorId(id));
    }

    // Ejemplo: GET /api/plantas/categoria/1
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<PlantaDTO>> buscarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(plantaService.buscarPorCategoria(categoriaId));
    }

    // Ejemplo: GET /api/plantas/buscar?nombre=mon
    @GetMapping("/buscar")
    public ResponseEntity<List<PlantaDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(plantaService.buscarPorNombre(nombre));
    }

    // Ejemplo: GET /api/plantas/luz/sombra
    @GetMapping("/luz/{nivelLuz}")
    public ResponseEntity<List<PlantaDTO>> buscarPorNivelLuz(@PathVariable String nivelLuz) {
        return ResponseEntity.ok(plantaService.buscarPorNivelLuz(nivelLuz));
    }

    // Ejemplo: GET /api/plantas/poco-stock?minimo=10
    @GetMapping("/poco-stock")
    public ResponseEntity<List<PlantaDTO>> listarConPocoStock(
            @RequestParam(defaultValue = "5") @Min(value = 0, message = "El minimo no puede ser negativo") Integer minimo) {
        return ResponseEntity.ok(plantaService.listarConPocoStock(minimo));
    }

    @PostMapping
    public ResponseEntity<PlantaDTO> crear(@Valid @RequestBody PlantaDTO dto) {
        return new ResponseEntity<>(plantaService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PlantaDTO dto) {
        return ResponseEntity.ok(plantaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        plantaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
