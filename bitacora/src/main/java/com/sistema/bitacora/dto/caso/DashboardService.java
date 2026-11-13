package com.sistema.bitacora.dto.caso;

import java.time.LocalDateTime;

public interface DashboardService {
    DashboardResponse obtenerEstadisticas();
    DashboardResponse obtenerEstadisticas(Long servicioId, Long estadoId, String termino,
                                          LocalDateTime fechaDesde, LocalDateTime fechaHasta);
}
