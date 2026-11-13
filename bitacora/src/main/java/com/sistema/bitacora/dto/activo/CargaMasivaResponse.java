package com.sistema.bitacora.dto.activo;

import java.util.List;

public record CargaMasivaResponse(
        int totalLineas,
        int creados,
        int omitidos,
        int errores,
        List<String> detalles
) {}