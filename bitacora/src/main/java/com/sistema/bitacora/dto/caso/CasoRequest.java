package com.sistema.bitacora.dto.caso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CasoRequest {

    @NotNull
    private LocalDateTime fechaSolicitud;

    @NotNull
    private Long servicioId;

    @NotNull
    private Long tipoServicioId;

    @NotBlank
    private String numeroCasoInt;

    private String numeroCasoESolution;

    @NotNull
    private Long estadoId;

    @NotNull
    private Long sedeId;

    @NotBlank
    private String descripcionFalla;

    private Long responsableId;
    private Long agenteRecibeId;
    private Long agenteResolutorId;

    private LocalDateTime fechaSolucion;
    private String observaciones;
}