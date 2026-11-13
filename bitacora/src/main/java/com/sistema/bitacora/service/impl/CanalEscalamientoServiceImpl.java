package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.canal.CanalEscalamientoResponse;
import com.sistema.bitacora.entity.CanalEscalamiento;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.repository.CanalEscalamientoRepository;
import com.sistema.bitacora.service.CanalEscalamientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CanalEscalamientoServiceImpl implements CanalEscalamientoService {

    private final CanalEscalamientoRepository canalRepository;

    @Override
    public List<CanalEscalamientoResponse> listarTodos() {
        return canalRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CanalEscalamientoResponse> listarActivos() {
        return canalRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CanalEscalamientoResponse crear(String nombre, String descripcion) {
        if (canalRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new IllegalArgumentException("Ya existe un canal con ese nombre");
        }
        CanalEscalamiento canal = CanalEscalamiento.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .activo(true)
                .build();
        return toResponse(canalRepository.save(canal));
    }

    @Override
    @Transactional
    public CanalEscalamientoResponse actualizar(Long id, String nombre, String descripcion, Boolean activo) {
        CanalEscalamiento canal = canalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado"));
        canal.setNombre(nombre);
        canal.setDescripcion(descripcion);
        canal.setActivo(activo != null ? activo : canal.getActivo());
        return toResponse(canalRepository.save(canal));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!canalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Canal no encontrado");
        }
        canalRepository.deleteById(id);
    }

    private CanalEscalamientoResponse toResponse(CanalEscalamiento canal) {
        return CanalEscalamientoResponse.builder()
                .id(canal.getId())
                .nombre(canal.getNombre())
                .descripcion(canal.getDescripcion())
                .activo(canal.getActivo())
                .build();
    }
}