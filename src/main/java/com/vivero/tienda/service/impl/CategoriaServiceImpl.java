package com.vivero.tienda.service.impl;

import com.vivero.tienda.dto.CategoriaDTO;
import com.vivero.tienda.entity.Categoria;
import com.vivero.tienda.exception.RecursoNoEncontradoException;
import com.vivero.tienda.exception.ReglaNegocioException;
import com.vivero.tienda.mapper.CategoriaMapper;
import com.vivero.tienda.repository.CategoriaRepository;
import com.vivero.tienda.repository.PlantaRepository;
import com.vivero.tienda.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * readOnly = true a nivel de clase: por defecto todos los metodos son de solo lectura
 * (Hibernate no revisa cambios ni hace flush). Los que escriben lo indican con @Transactional.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final PlantaRepository plantaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public List<CategoriaDTO> listar() {
        return categoriaRepository.listarConCantidadPlantas();
    }

    @Override
    public CategoriaDTO buscarPorId(Long id) {
        Categoria categoria = obtenerEntidad(id);
        return categoriaMapper.toDTO(categoria, plantaRepository.countByCategoriaId(id));
    }

    @Override
    @Transactional
    public CategoriaDTO crear(CategoriaDTO dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new ReglaNegocioException("Ya existe una categoria con el nombre: " + dto.getNombre());
        }
        Categoria guardada = categoriaRepository.save(categoriaMapper.toEntity(dto));
        return categoriaMapper.toDTO(guardada, 0);
    }

    @Override
    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = obtenerEntidad(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre().trim(), id)) {
            throw new ReglaNegocioException("Ya existe otra categoria con el nombre: " + dto.getNombre());
        }
        // No hace falta save(): la entidad esta gestionada y Hibernate guarda los cambios al terminar
        categoriaMapper.actualizarEntidad(dto, categoria);
        return categoriaMapper.toDTO(categoria, plantaRepository.countByCategoriaId(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerEntidad(id);
        if (plantaRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar una categoria que todavia tiene plantas");
        }
        categoriaRepository.delete(categoria);
    }

    @Override
    public Categoria obtenerEntidad(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria == null) {
            throw new RecursoNoEncontradoException("No existe la categoria con id: " + id);
        }
        return categoria;
    }
}
