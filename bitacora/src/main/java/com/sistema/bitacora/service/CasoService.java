package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CasoService {
    CasoResponse crearCaso(CasoRequest request);
    List<CasoResponse> listarCasos();
    List<CasoResponse> buscarCasosPorSede(String termino);
    String generarNumeroCasoAutomatico(String nombreServicio, String tipo);
    CasoResponse obtenerCasoPorId(Long id);
    CasoResponse actualizarCaso(Long id, CasoRequest request);
    void eliminarCaso(Long id);
    Page<CasoResponse> listarCasosPaginado(String termino, int pagina, int tamanio);
}