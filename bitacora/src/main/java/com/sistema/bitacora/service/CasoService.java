package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;

import java.util.List;

public interface CasoService {
    CasoResponse crearCaso(CasoRequest request);
    List<CasoResponse> listarCasos();
    CasoResponse obtenerCasoPorId(Long id);
    CasoResponse actualizarCaso(Long id, CasoRequest request);
}