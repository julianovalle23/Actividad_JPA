package com.vivero.tienda.repository;

import com.vivero.tienda.entity.DetallePedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Override
    @EntityGraph(attributePaths = "planta")
    Optional<DetallePedido> findById(Long id);

    @EntityGraph(attributePaths = "planta")
    List<DetallePedido> findByPedidoId(Long pedidoId);

    boolean existsByPlantaId(Long plantaId);
}
