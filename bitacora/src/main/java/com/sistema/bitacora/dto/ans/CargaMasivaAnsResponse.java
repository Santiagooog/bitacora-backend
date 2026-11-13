package com.sistema.bitacora.dto.ans;

import java.util.List;

public record CargaMasivaAnsResponse(
    int totalLineas,
    int creados,
    int omitidos,
    int errores,
    List<String> detalles
) {}