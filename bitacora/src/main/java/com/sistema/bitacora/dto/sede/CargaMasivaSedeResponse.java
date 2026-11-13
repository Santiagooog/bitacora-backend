package com.sistema.bitacora.dto.sede;

import java.util.List;

public record CargaMasivaSedeResponse(
        int totalLineas,
        int creados,
        int omitidos,
        int errores,
        List<String> detalles
) {}