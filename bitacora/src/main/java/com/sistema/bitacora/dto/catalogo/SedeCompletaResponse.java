package com.sistema.bitacora.dto.catalogo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SedeCompletaResponse {
    private Long id;
    private String sban;
    private String nombreSede;
    private String municipio;
    private String regional;
    private String departamento;
    private String direccion;
    private String horario;
    private String atencion;
    private Boolean activa;
}