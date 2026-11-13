package com.sistema.bitacora.dto.categoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaSolucionResponse {
    private Long id;
    private Long servicioId;
    private String nombre;
    private Boolean activo;
}