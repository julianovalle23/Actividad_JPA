package com.vivero.tienda.service;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica que los listados se resuelven con UNA sola consulta SQL,
 * sin importar cuantos registros haya (sin problema N+1).
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class ConsultasSinN1Test {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlantaService plantaService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private PedidoService pedidoService;

    private Statistics estadisticas;

    @BeforeEach
    void reiniciarContador() {
        estadisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();
    }

    @Test
    void listarPlantas_unaSolaConsulta() {
        plantaService.listar();
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }

    @Test
    void listarCategoriasConCantidadDePlantas_unaSolaConsulta() {
        categoriaService.listar();
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }

    @Test
    void listarPedidosConClienteDetallesYPlantas_unaSolaConsulta() {
        pedidoService.listar();
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }
}
