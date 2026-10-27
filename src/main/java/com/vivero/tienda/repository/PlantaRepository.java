package com.vivero.tienda.repository;

import com.vivero.tienda.entity.NivelLuz;
import com.vivero.tienda.entity.Planta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @EntityGraph(attributePaths = "categoria") hace un JOIN con la categoria en la
 * misma consulta. Sin el, el mapper dispararia una consulta extra por cada planta (N+1).
 */
@Repository
public interface PlantaRepository extends JpaRepository<Planta, Long> {

    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Planta> findAll();

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Planta> findById(Long id);

    @EntityGraph(attributePaths = "categoria")
    List<Planta> findByCategoriaId(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    List<Planta> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = "categoria")
    List<Planta> findByNivelLuz(NivelLuz nivelLuz);

    // Consulta JPQL escrita a mano: JOIN FETCH cumple la misma funcion que @EntityGraph
    @Query("SELECT p FROM Planta p JOIN FETCH p.categoria WHERE p.stock <= :minimo ORDER BY p.stock")
    List<Planta> buscarConPocoStock(@Param("minimo") Integer minimo);

    long countByCategoriaId(Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);
}
