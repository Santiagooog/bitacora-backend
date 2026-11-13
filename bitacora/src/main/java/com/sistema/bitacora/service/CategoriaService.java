package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.categoria.CategoriaFallaResponse;
import com.sistema.bitacora.dto.categoria.CategoriaSolucionResponse;
import com.sistema.bitacora.entity.CategoriaFalla;
import com.sistema.bitacora.entity.CategoriaSolucion;
import com.sistema.bitacora.entity.Servicio;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.repository.CategoriaFallaRepository;
import com.sistema.bitacora.repository.CategoriaSolucionRepository;
import com.sistema.bitacora.repository.ServicioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaFallaRepository categoriaFallaRepository;
    private final CategoriaSolucionRepository categoriaSolucionRepository;
    private final ServicioRepository servicioRepository;

    // ========== FALLAS ==========
    public List<CategoriaFallaResponse> listarFallas(Long servicioId) {
        return categoriaFallaRepository.findByServicioIdOrderByNombreAsc(servicioId)
                .stream()
                .map(this::toFallaResponse)
                .toList();
    }

    @Transactional
    public CategoriaFallaResponse crearFalla(Long servicioId, String nombre) {
        if (categoriaFallaRepository.existsByServicioIdAndNombreIgnoreCase(servicioId, nombre)) {
            throw new IllegalArgumentException("Ya existe esa categoría de falla");
        }
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        CategoriaFalla falla = CategoriaFalla.builder()
                .servicio(servicio)
                .nombre(nombre)
                .activo(true)
                .build();
        return toFallaResponse(categoriaFallaRepository.save(falla));
    }

    @Transactional
    public void eliminarFalla(Long id) {
        if (!categoriaFallaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría de falla no encontrada");
        }
        categoriaFallaRepository.deleteById(id);
    }

    // ========== SOLUCIONES ==========
    public List<CategoriaSolucionResponse> listarSoluciones(Long servicioId) {
        return categoriaSolucionRepository.findByServicioIdOrderByNombreAsc(servicioId)
                .stream()
                .map(this::toSolucionResponse)
                .toList();
    }

    @Transactional
    public CategoriaSolucionResponse crearSolucion(Long servicioId, String nombre) {
        if (categoriaSolucionRepository.existsByServicioIdAndNombreIgnoreCase(servicioId, nombre)) {
            throw new IllegalArgumentException("Ya existe esa categoría de solución");
        }
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        CategoriaSolucion solucion = CategoriaSolucion.builder()
                .servicio(servicio)
                .nombre(nombre)
                .activo(true)
                .build();
        return toSolucionResponse(categoriaSolucionRepository.save(solucion));
    }

    @Transactional
    public void eliminarSolucion(Long id) {
        if (!categoriaSolucionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría de solución no encontrada");
        }
        categoriaSolucionRepository.deleteById(id);
    }

    // ========== MAPPERS ==========
    private CategoriaFallaResponse toFallaResponse(CategoriaFalla falla) {
        return CategoriaFallaResponse.builder()
                .id(falla.getId())
                .servicioId(falla.getServicio().getId())
                .servicioNombre(falla.getServicio().getNombre())
                .nombre(falla.getNombre())
                .activo(falla.getActivo())
                .build();
    }

    private CategoriaSolucionResponse toSolucionResponse(CategoriaSolucion solucion) {
        return CategoriaSolucionResponse.builder()
                .id(solucion.getId())
                .servicioId(solucion.getServicio().getId())
                .nombre(solucion.getNombre())
                .activo(solucion.getActivo())
                .build();
    }
}