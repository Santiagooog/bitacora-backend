package com.sistema.bitacora.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private Boolean activo;
    private List<Long> serviciosPermitidosIds;
    private List<String> serviciosPermitidos;
}
