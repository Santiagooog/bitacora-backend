package com.sistema.bitacora.mapper;

import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.entity.Caso;
import org.springframework.stereotype.Component;

@Component
public class CasoMapper {

    public CasoResponse toResponse(Caso caso) {
        return CasoResponse.builder()
                .id(caso.getId())
                .fechaSolicitud(caso.getFechaSolicitud())
                .servicio(caso.getServicio() != null ? caso.getServicio().getNombre() : null)
                .tipoServicio(caso.getTipoServicio() != null ? caso.getTipoServicio().getNombre() : null)
                .numeroCasoInt(caso.getNumeroCasoInt())
                .numeroCasoESolution(caso.getNumeroCasoESolution())
                .estado(caso.getEstado() != null ? caso.getEstado().getNombre() : null)
                .sede(caso.getSedePrincipal() != null ? caso.getSedePrincipal().getNombreSede() : null)
                .descripcionFalla(caso.getDescripcionFalla())
                .responsable(caso.getResponsable() != null ? caso.getResponsable().getNombre() : null)
                .agenteRecibe(caso.getAgenteRecibe() != null ? caso.getAgenteRecibe().getNombre() : null)
                .agenteResolutor(caso.getAgenteResolutor() != null ? caso.getAgenteResolutor().getNombre() : null)
                .fechaSolucion(caso.getFechaSolucion())
                .observaciones(caso.getObservaciones())
                .fechaCreacion(caso.getFechaCreacion())
                .fechaActualizacion(caso.getFechaActualizacion())
                .build();
    }
}