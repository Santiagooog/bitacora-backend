package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.entity.*;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.mapper.CasoMapper;
import com.sistema.bitacora.repository.*;
import com.sistema.bitacora.service.CasoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CasoServiceImpl implements CasoService {

    private final CasoRepository casoRepository;
    private final ServicioRepository servicioRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EstadoCasoRepository estadoCasoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CasoMapper casoMapper;

    @Override
    public CasoResponse crearCaso(CasoRequest request) {
        if (casoRepository.existsByNumeroCasoInt(request.getNumeroCasoInt())) {
            throw new IllegalArgumentException("Ya existe un caso con ese número interno.");
        }

        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        TipoServicio tipoServicio = tipoServicioRepository.findById(request.getTipoServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado"));

        EstadoCaso estado = estadoCasoRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado"));

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Usuario responsable = request.getResponsableId() != null
                ? usuarioRepository.findById(request.getResponsableId())
                  .orElseThrow(() -> new ResourceNotFoundException("Responsable no encontrado"))
                : null;

        Usuario agenteRecibe = request.getAgenteRecibeId() != null
                ? usuarioRepository.findById(request.getAgenteRecibeId())
                  .orElseThrow(() -> new ResourceNotFoundException("Agente que recibe no encontrado"))
                : null;

        Usuario agenteResolutor = request.getAgenteResolutorId() != null
                ? usuarioRepository.findById(request.getAgenteResolutorId())
                  .orElseThrow(() -> new ResourceNotFoundException("Agente resolutor no encontrado"))
                : null;

        Caso caso = Caso.builder()
                .fechaSolicitud(request.getFechaSolicitud())
                .servicio(servicio)
                .tipoServicio(tipoServicio)
                .numeroCasoInt(request.getNumeroCasoInt())
                .numeroCasoESolution(request.getNumeroCasoESolution())
                .estado(estado)
                .sedePrincipal(sede)
                .descripcionFalla(request.getDescripcionFalla())
                .responsable(responsable)
                .agenteRecibe(agenteRecibe)
                .agenteResolutor(agenteResolutor)
                .fechaSolucion(request.getFechaSolucion())
                .observaciones(request.getObservaciones())
                .build();

        Caso guardado = casoRepository.save(caso);
        return casoMapper.toResponse(guardado);
    }

    @Override
    public List<CasoResponse> listarCasos() {
        return casoRepository.findAll()
                .stream()
                .map(casoMapper::toResponse)
                .toList();
    }

    @Override
    public CasoResponse obtenerCasoPorId(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        return casoMapper.toResponse(caso);
    }

    @Override
    public CasoResponse actualizarCaso(Long id, CasoRequest request) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        TipoServicio tipoServicio = tipoServicioRepository.findById(request.getTipoServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado"));

        EstadoCaso estado = estadoCasoRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado"));

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Usuario responsable = request.getResponsableId() != null
                ? usuarioRepository.findById(request.getResponsableId())
                  .orElseThrow(() -> new ResourceNotFoundException("Responsable no encontrado"))
                : null;

        Usuario agenteRecibe = request.getAgenteRecibeId() != null
                ? usuarioRepository.findById(request.getAgenteRecibeId())
                  .orElseThrow(() -> new ResourceNotFoundException("Agente que recibe no encontrado"))
                : null;

        Usuario agenteResolutor = request.getAgenteResolutorId() != null
                ? usuarioRepository.findById(request.getAgenteResolutorId())
                  .orElseThrow(() -> new ResourceNotFoundException("Agente resolutor no encontrado"))
                : null;

        caso.setFechaSolicitud(request.getFechaSolicitud());
        caso.setServicio(servicio);
        caso.setTipoServicio(tipoServicio);
        caso.setNumeroCasoInt(request.getNumeroCasoInt());
        caso.setNumeroCasoESolution(request.getNumeroCasoESolution());
        caso.setEstado(estado);
        caso.setSedePrincipal(sede);
        caso.setDescripcionFalla(request.getDescripcionFalla());
        caso.setResponsable(responsable);
        caso.setAgenteRecibe(agenteRecibe);
        caso.setAgenteResolutor(agenteResolutor);
        caso.setFechaSolucion(request.getFechaSolucion());
        caso.setObservaciones(request.getObservaciones());

        Caso actualizado = casoRepository.save(caso);
        return casoMapper.toResponse(actualizado);
    }
}