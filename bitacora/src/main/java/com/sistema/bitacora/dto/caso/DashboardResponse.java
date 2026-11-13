package com.sistema.bitacora.dto.caso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private long totalCasos;
    private Map<String, Long> casosPorEstado;
    private Map<String, Long> casosPorServicio;
    private Map<String, Long> casosPorSede;
    private Map<String, Long> casosPorMes;
    private long casosActivos;
    private long casosEscalados;
    private long ansVencidos;
    private List<CasoResponse> casosRecientes;
}
