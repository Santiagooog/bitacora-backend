package com.sistema.bitacora.dto.historial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistorialCasoResponse {
    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private String apellidoUsuario;
    private String accion;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private String observacion;
    private LocalDateTime fecha;
}