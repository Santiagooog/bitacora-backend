package com.sistema.bitacora.dto.caso;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CasoResponse {
    private Long id;
    private LocalDateTime fechaSolicitud;

    // IDs para editar
    private Long servicioId;
    private Long tipoServicioId;
    private Long estadoId;
    private Long sedeId;
    private Long creadoPorId;
    private String creadoPorNombre;
    private String creadoPorApellido;
    private Long actualizadoPorId;
    private String actualizadoPorNombre;
    private String actualizadoPorApellido;
    private String servicio;
    private String tipoServicio;
    private String numeroCasoInt;
    private String numeroCasoESolution;
    private String estado;
    private String sede;
    private String sban;
    private String proyecto;
    private String descripcionFalla;
    private String responsable;
    private String agenteRecibe;
    private String agenteResolutor;
    private LocalDateTime fechaSolucion;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long categoriaFallaId;
    private String categoriaFallaNombre;
    private Long categoriaSolucionId;
    private String categoriaSolucionNombre;
    private LocalDateTime fechaVencimiento;
    private Long segundosAnsRestantes;
    private String equipo;
    private String modelo;
    private String serial;
    private String placa;
    private String canalEscalamiento;
    private Long activoId;
    private String activoSerial;
    private String activoPlaca;
    private String activoName;
    private String activoDeviceType;
    private String activoTipoServicio;
    private String grupoEscalamiento;
    private String escaladoA;

}
