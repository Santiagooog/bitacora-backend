package com.sistema.bitacora.dto.caso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}