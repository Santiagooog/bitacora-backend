package com.sistema.bitacora.dto.puntoAcceso;

import java.util.List;

public record CargaMasivaPuntoResponse(
        int totalLineas,
        int creados,
        int omitidos,
        int errores,
        List<String> detalles
) {}