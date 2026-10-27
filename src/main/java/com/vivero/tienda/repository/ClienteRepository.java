package com.vivero.tienda.repository;

import com.vivero.tienda.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmailIgnoreCase(String email);

    // Para validar el email al actualizar sin chocar con el propio cliente
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
