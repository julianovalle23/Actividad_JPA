package com.vivero.tienda.mapper;

import com.vivero.tienda.dto.PlantaDTO;
import com.vivero.tienda.entity.Planta;
import org.springframework.stereotype.Component;

@Component
public class PlantaMapper {

    /**
     * Requiere que la categoria de la planta ya este cargada
     * (los metodos de PlantaRepository la traen con @EntityGraph).
     */
    public PlantaDTO toDTO(Planta planta) {
        PlantaDTO dto = new PlantaDTO();
        dto.setId(planta.getId());
        dto.setNombre(planta.getNombre());
        dto.setNombreCientifico(planta.getNombreCientifico());
        dto.setPrecio(planta.getPrecio());
        dto.setStock(planta.getStock());
        dto.setNivelLuz(planta.getNivelLuz());
        dto.setRiegoCadaDias(planta.getRiegoCadaDias());
        dto.setCategoriaId(planta.getCategoria().getId());
        dto.setCategoriaNombre(planta.getCategoria().getNombre());
        return dto;
    }

    // La categoria NO se asigna aqui: la busca el servicio en la base de datos
    public Planta toEntity(PlantaDTO dto) {
        Planta planta = new Planta();
        actualizarEntidad(dto, planta);
        return planta;
    }

    // Copia los datos editables del DTO a una entidad ya existente
    public void actualizarEntidad(PlantaDTO dto, Planta planta) {
        planta.setNombre(dto.getNombre().trim());
        planta.setNombreCientifico(dto.getNombreCientifico());
        planta.setPrecio(dto.getPrecio());
        planta.setStock(dto.getStock());
        planta.setNivelLuz(dto.getNivelLuz());
        planta.setRiegoCadaDias(dto.getRiegoCadaDias());
    }
}
