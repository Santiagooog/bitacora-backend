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
public class CasoRequest {
    private LocalDateTime fechaSolicitud;
    private Long servicioId;
    private Long tipoServicioId;
    private String numeroCasoInt;
    private String numeroCasoESolution;
    private Long estadoId;
    private Long sedeId;
    private String descripcionFalla;
    private Long responsableId;
    private Long agenteRecibeId;
    private Long agenteResolutorId;
    private LocalDateTime fechaSolucion;
    private String observaciones;
    private Long categoriaFallaId;
    private Long categoriaSolucionId;
    private String equipo;
    private String modelo;
    private String serial;
    private String placa;
    private String serialNuevo;
    private String placaNueva;
    private Long activoId;
    private String canalEscalamiento;
    private String grupoEscalamiento;
    private String escaladoA;


}
