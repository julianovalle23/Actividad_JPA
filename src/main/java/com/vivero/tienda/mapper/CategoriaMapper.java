package com.vivero.tienda.mapper;

import com.vivero.tienda.dto.CategoriaDTO;
import com.vivero.tienda.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    /**
     * El numero de plantas se recibe aparte (se calcula con una consulta COUNT)
     * para no tener que cargar la coleccion completa de plantas.
     */
    public CategoriaDTO toDTO(Categoria categoria, long cantidadPlantas) {
        return new CategoriaDTO(categoria.getId(), categoria.getNombre(),
                categoria.getDescripcion(), cantidadPlantas);
    }

    public Categoria toEntity(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        actualizarEntidad(dto, categoria);
        return categoria;
    }

    // Copia los datos editables del DTO a una entidad ya existente
    public void actualizarEntidad(CategoriaDTO dto, Categoria categoria) {
        categoria.setNombre(dto.getNombre().trim());
        categoria.setDescripcion(dto.getDescripcion());
    }
}
