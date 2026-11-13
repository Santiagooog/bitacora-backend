package com.sistema.bitacora.dto.caso;

import com.sistema.bitacora.dto.historial.HistorialCasoResponse;
import java.util.List;

public record ExportacionCasosResponse(
        List<CasoResponse> casos,
        List<HistorialCasoResponse> historial
) {}