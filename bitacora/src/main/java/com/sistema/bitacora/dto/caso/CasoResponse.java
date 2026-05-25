package com.sistema.bitacora.dto.caso;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CasoResponse {

    private Long id;
    private LocalDateTime fechaSolicitud;
    private String servicio;
    private String tipoServicio;
    private String numeroCasoInt;
    private String numeroCasoESolution;
    private String estado;
    private String sede;
    private String descripcionFalla;
    private String responsable;
    private String agenteRecibe;
    private String agenteResolutor;
    private LocalDateTime fechaSolucion;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}