package com.vivero.tienda.repository;

import com.vivero.tienda.dto.CategoriaDTO;
import com.vivero.tienda.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    // Para validar el nombre al actualizar sin chocar con la propia categoria
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /*
     * Proyeccion a DTO: en UNA sola consulta trae cada categoria con su numero de plantas.
     * Evita el N+1 de hacer categoria.getPlantas().size() por cada categoria.
     */
    @Query("SELECT new com.vivero.tienda.dto.CategoriaDTO(c.id, c.nombre, c.descripcion, COUNT(p)) "
            + "FROM Categoria c LEFT JOIN c.plantas p "
            + "GROUP BY c.id, c.nombre, c.descripcion "
            + "ORDER BY c.nombre")
    List<CategoriaDTO> listarConCantidadPlantas();
}
