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

                // IDs para editar
                .servicioId(caso.getServicio() != null ? caso.getServicio().getId() : null)
                .tipoServicioId(caso.getTipoServicio() != null ? caso.getTipoServicio().getId() : null)
                .estadoId(caso.getEstado() != null ? caso.getEstado().getId() : null)
                .sedeId(caso.getSedePrincipal() != null ? caso.getSedePrincipal().getId() : null)

                // Nombres para mostrar
                .servicio(caso.getServicio() != null ? caso.getServicio().getNombre() : null)
                .tipoServicio(caso.getTipoServicio() != null ? caso.getTipoServicio().getNombre() : null)
                .numeroCasoInt(caso.getNumeroCasoInt())
                .numeroCasoESolution(caso.getNumeroCasoESolution())
                .estado(caso.getEstado() != null ? caso.getEstado().getNombre() : null)
                .sede(caso.getSedePrincipal() != null ? caso.getSedePrincipal().getNombreSede() : null)
                .sban(caso.getSedePrincipal() != null ? caso.getSedePrincipal().getSban() : null)
                .descripcionFalla(caso.getDescripcionFalla())

                // Agentes
                .responsable(caso.getResponsable() != null ? caso.getResponsable().getNombre() : null)
                .agenteRecibe(caso.getAgenteRecibe() != null ? caso.getAgenteRecibe().getNombre() : null)
                .agenteResolutor(caso.getAgenteResolutor() != null ? caso.getAgenteResolutor().getNombre() : null)

                .fechaSolucion(caso.getFechaSolucion())
                .observaciones(caso.getObservaciones())
                .fechaCreacion(caso.getFechaCreacion())
                .fechaActualizacion(caso.getFechaActualizacion())
                .creadoPorId(caso.getCreadoPor() != null ? caso.getCreadoPor().getId() : null)
                .creadoPorNombre(caso.getCreadoPor() != null ? caso.getCreadoPor().getNombre() : null)
                .creadoPorApellido(caso.getCreadoPor() != null ? caso.getCreadoPor().getApellido() : null)
                .actualizadoPorId(caso.getActualizadoPor() != null ? caso.getActualizadoPor().getId() : null)
                .actualizadoPorNombre(caso.getActualizadoPor() != null ? caso.getActualizadoPor().getNombre() : null)
                .actualizadoPorApellido(caso.getActualizadoPor() != null ? caso.getActualizadoPor().getApellido() : null)
                .build();
    }
}