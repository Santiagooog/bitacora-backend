package com.sistema.bitacora.dto.caso;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    private long totalCasos;
    private long casosEnProceso;
    private long casosSolucionados;
    private long casosEscalados;
}