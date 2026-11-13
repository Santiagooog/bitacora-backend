package com.sistema.bitacora.dto.canal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CanalEscalamientoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}