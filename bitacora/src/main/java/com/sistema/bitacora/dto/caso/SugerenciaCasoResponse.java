package com.sistema.bitacora.dto.caso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SugerenciaCasoResponse {
    private Long id;
    private String numeroCasoInt;
    private String descripcionFalla;
    private String observaciones; // solución o notas
    private String estado;
    private String servicio;
    private String sede;
    private LocalDateTime fechaSolucion;
    private String categoriaFallaNombre;
    private String categoriaSolucionNombre;
}