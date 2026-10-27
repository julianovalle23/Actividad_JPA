package com.vivero.tienda.service.impl;

import com.vivero.tienda.dto.PlantaDTO;
import com.vivero.tienda.entity.NivelLuz;
import com.vivero.tienda.entity.Planta;
import com.vivero.tienda.exception.RecursoNoEncontradoException;
import com.vivero.tienda.exception.ReglaNegocioException;
import com.vivero.tienda.mapper.PlantaMapper;
import com.vivero.tienda.repository.DetallePedidoRepository;
import com.vivero.tienda.repository.PlantaRepository;
import com.vivero.tienda.service.CategoriaService;
import com.vivero.tienda.service.PlantaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantaServiceImpl implements PlantaService {

    private final PlantaRepository plantaRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CategoriaService categoriaService;
    private final PlantaMapper plantaMapper;

    @Override
    public List<PlantaDTO> listar() {
        return convertirLista(plantaRepository.findAll());
    }

    @Override
    public PlantaDTO buscarPorId(Long id) {
        return plantaMapper.toDTO(obtenerEntidad(id));
    }

    @Override
    public List<PlantaDTO> buscarPorCategoria(Long categoriaId) {
        categoriaService.obtenerEntidad(categoriaId); // 404 si la categoria no existe
        return convertirLista(plantaRepository.findByCategoriaId(categoriaId));
    }

    @Override
    public List<PlantaDTO> buscarPorNombre(String nombre) {
        return convertirLista(plantaRepository.findByNombreContainingIgnoreCase(nombre.trim()));
    }

    @Override
    public List<PlantaDTO> buscarPorNivelLuz(String nivelLuz) {
        return convertirLista(plantaRepository.findByNivelLuz(NivelLuz.desdeTexto(nivelLuz)));
    }

    @Override
    public List<PlantaDTO> listarConPocoStock(Integer minimo) {
        return convertirLista(plantaRepository.buscarConPocoStock(minimo));
    }

    @Override
    @Transactional
    public PlantaDTO crear(PlantaDTO dto) {
        Planta planta = plantaMapper.toEntity(dto);
        planta.setCategoria(categoriaService.obtenerEntidad(dto.getCategoriaId()));
        return plantaMapper.toDTO(plantaRepository.save(planta));
    }

    @Override
    @Transactional
    public PlantaDTO actualizar(Long id, PlantaDTO dto) {
        Planta planta = obtenerEntidad(id);
        plantaMapper.actualizarEntidad(dto, planta);
        planta.setCategoria(categoriaService.obtenerEntidad(dto.getCategoriaId()));
        return plantaMapper.toDTO(planta);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Planta planta = obtenerEntidad(id);
        if (detallePedidoRepository.existsByPlantaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la planta '" + planta.getNombre()
                    + "' porque aparece en pedidos. Puedes dejar su stock en 0");
        }
        plantaRepository.delete(planta);
    }

    @Override
    public Planta obtenerEntidad(Long id) {
        Planta planta = plantaRepository.findById(id).orElse(null);
        if (planta == null) {
            throw new RecursoNoEncontradoException("No existe la planta con id: " + id);
        }
        return planta;
    }

    private List<PlantaDTO> convertirLista(List<Planta> plantas) {
        List<PlantaDTO> resultado = new ArrayList<>();
        for (Planta planta : plantas) {
            resultado.add(plantaMapper.toDTO(planta));
        }
        return resultado;
    }
}
