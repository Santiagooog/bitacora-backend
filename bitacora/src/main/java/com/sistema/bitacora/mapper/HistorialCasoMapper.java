package com.sistema.bitacora.mapper;

import com.sistema.bitacora.dto.historial.HistorialCasoResponse;
import com.sistema.bitacora.entity.HistorialCaso;
import org.springframework.stereotype.Component;

@Component
public class HistorialCasoMapper {

    public HistorialCasoResponse toResponse(HistorialCaso historial) {
        String nombreUsuario = null;
        String apellidoUsuario = null;
        Long usuarioId = null;

        if (historial.getUsuario() != null) {
            nombreUsuario = historial.getUsuario().getNombre();
            apellidoUsuario = historial.getUsuario().getApellido();
            usuarioId = historial.getUsuario().getId();
        }

        return HistorialCasoResponse.builder()
                .id(historial.getId())
                .accion(historial.getAccion())
                .campoModificado(historial.getCampoModificado())
                .valorAnterior(historial.getValorAnterior())
                .valorNuevo(historial.getValorNuevo())
                .observacion(historial.getObservacion())
                .fecha(historial.getFecha())
                .usuarioId(usuarioId)
                .nombreUsuario(nombreUsuario)
                .apellidoUsuario(apellidoUsuario)
                .build();
    }
}