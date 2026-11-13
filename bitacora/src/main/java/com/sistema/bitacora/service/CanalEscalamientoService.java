package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.canal.CanalEscalamientoResponse;

import java.util.List;

public interface CanalEscalamientoService {
    List<CanalEscalamientoResponse> listarTodos();
    List<CanalEscalamientoResponse> listarActivos();
    CanalEscalamientoResponse crear(String nombre, String descripcion);
    CanalEscalamientoResponse actualizar(Long id, String nombre, String descripcion, Boolean activo);
    void eliminar(Long id);
}