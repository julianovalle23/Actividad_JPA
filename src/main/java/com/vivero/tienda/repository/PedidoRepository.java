package com.vivero.tienda.repository;

import com.vivero.tienda.entity.EstadoPedido;
import com.vivero.tienda.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Para mostrar un pedido necesitamos: cliente + detalles + planta de cada detalle.
 * El @EntityGraph trae todo eso en UNA consulta en lugar de 1 + N + N*M.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.planta"})
    List<Pedido> findAll();

    @Override
    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.planta"})
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.planta"})
    List<Pedido> findByClienteIdOrderByFechaDesc(Long clienteId);

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.planta"})
    List<Pedido> findByEstado(EstadoPedido estado);

    boolean existsByClienteId(Long clienteId);
}
