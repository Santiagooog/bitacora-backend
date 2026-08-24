package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.entity.*;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.mapper.CasoMapper;
import com.sistema.bitacora.repository.*;
import com.sistema.bitacora.service.CasoService;
import com.sistema.bitacora.service.HistorialCasoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Objects;

@Slf4j
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
    private final HistorialCasoService historialService;
    private final HistorialCasoRepository historialCasoRepository;
    @PersistenceContext
    private EntityManager entityManager;

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

        Usuario usuarioActual = getUsuarioAutenticado();
        caso.setCreadoPor(usuarioActual);

        Caso guardado = casoRepository.save(caso);

        // ✅ REGISTRAR EN HISTORIAL (con try-catch para no romper el flujo)
        try {
            historialService.registrar(
                    guardado.getId(),
                    "CREAR",
                    null,
                    null,
                    null,
                    "Caso creado con número " + guardado.getNumeroCasoInt()
            );
        } catch (Exception e) {
            log.warn("No se pudo registrar historial al crear caso: {}", e.getMessage());
        }

        return casoMapper.toResponse(guardado);
    }

    @Override
    public List<CasoResponse> listarCasos() {
        return casoRepository.findAll()
                .stream()
                .map(casoMapper::toResponse)
                .toList();
    }
    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            return usuarioRepository.findByCorreo(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
        }
        return null;
    }

    @Override
    public List<CasoResponse> buscarCasosPorSede(String termino) {
        return casoRepository.buscarPorSede(termino)
                .stream()
                .map(casoMapper::toResponse)
                .toList();
    }

    @Override
    public String generarNumeroCasoAutomatico(String nombreServicio, String tipo) {
        String prefijoServicio = "";
        String nombreServicioUpper = nombreServicio.toUpperCase();

        if (nombreServicioUpper.contains("SDWAN")) {
            prefijoServicio = "SDWN";
        } else if (nombreServicioUpper.contains("WIFI PUBLICO") || nombreServicioUpper.contains("WIFI PÚBLICO")) {
            prefijoServicio = "WPB";
        } else if (nombreServicioUpper.contains("CONTROL DE ACCESOS") || nombreServicioUpper.contains("CONTROL DE ACCESO")) {
            prefijoServicio = "AC";
        } else if (nombreServicioUpper.contains("WIFI ADMINISTRATIVO")) {
            prefijoServicio = "WAM";
        } else {
            throw new IllegalArgumentException("Servicio no soportado para generación automática: " + nombreServicio);
        }

        String tipoUpper = tipo.toUpperCase();
        String tipoCaso;
        if (tipoUpper.contains("INCIDENTE") || tipoUpper.contains("INC")) {
            tipoCaso = "INC";
        } else if (tipoUpper.contains("REQUERIMIENTO") || tipoUpper.contains("REQ")) {
            tipoCaso = "REQ";
        } else {
            throw new IllegalArgumentException("Tipo debe ser INCIDENTE o REQUERIMIENTO");
        }

        String prefix = prefijoServicio + "-" + tipoCaso + "-";
        String ultimoNumero = casoRepository.findUltimoNumeroCaso(nombreServicio, prefix);

        int siguienteNumero = 1;
        if (ultimoNumero != null && !ultimoNumero.isEmpty()) {
            String numeroStr = ultimoNumero.substring(ultimoNumero.length() - 6);
            siguienteNumero = Integer.parseInt(numeroStr) + 1;
        }

        String numeroFormateado = String.format("%06d", siguienteNumero);
        return prefix + numeroFormateado;
    }

    @Override
    @Transactional
    public void eliminarCaso(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + id));

        // Primero eliminar el historial asociado
        historialCasoRepository.deleteByCasoId(id);

        // Luego eliminar el caso
        casoRepository.delete(caso);
    }

    @Override
    public CasoResponse obtenerCasoPorId(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        return casoMapper.toResponse(caso);
    }

    @Override
    public Page<CasoResponse> listarCasosPaginado(String termino, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("fechaSolicitud").descending());
        Page<Caso> casosPage = casoRepository.buscarCasosPaginado(termino, pageable);
        return casosPage.map(casoMapper::toResponse);
    }

    @Override
    @Transactional  // ← Agrega esto para que historial + guardado sean atómicos
    public CasoResponse actualizarCaso(Long id, CasoRequest request) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        // ═══════════════════════════════════════════════════════
        // 1. GUARDAR TODOS LOS VALORES ANTIGUOS ANTES DE MODIFICAR
        // ═══════════════════════════════════════════════════════
        String estadoAnterior = caso.getEstado() != null ? caso.getEstado().getNombre() : null;
        String servicioAnterior = caso.getServicio() != null ? caso.getServicio().getNombre() : null;
        String tipoServicioAnterior = caso.getTipoServicio() != null ? caso.getTipoServicio().getNombre() : null;
        String sedeAnterior = caso.getSedePrincipal() != null ? caso.getSedePrincipal().getNombreSede() : null;
        String descripcionAnterior = caso.getDescripcionFalla();
        String observacionesAnterior = caso.getObservaciones();
        String esolutionAnterior = caso.getNumeroCasoESolution();
        String numeroIntAnterior = caso.getNumeroCasoInt();
        String responsableAnterior = caso.getResponsable() != null ? caso.getResponsable().getNombre() : null;
        String agenteRecibeAnterior = caso.getAgenteRecibe() != null ? caso.getAgenteRecibe().getNombre() : null;
        String agenteResolutorAnterior = caso.getAgenteResolutor() != null ? caso.getAgenteResolutor().getNombre() : null;

        // ═══════════════════════════════════════════════════════
        // 2. APLICAR LAS MODIFICACIONES (tu lógica actual, sin cambios)
        // ═══════════════════════════════════════════════════════
        if (request.getServicioId() != null) {
            Servicio servicio = servicioRepository.findById(request.getServicioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
            caso.setServicio(servicio);
        }

        if (request.getTipoServicioId() != null) {
            TipoServicio tipoServicio = tipoServicioRepository.findById(request.getTipoServicioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado"));
            caso.setTipoServicio(tipoServicio);
        }

        if (request.getEstadoId() != null) {
            EstadoCaso estado = estadoCasoRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado"));
            caso.setEstado(estado);
        }

        if (request.getSedeId() != null) {
            Sede sede = sedeRepository.findById(request.getSedeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
            caso.setSedePrincipal(sede);
        }

        if (request.getResponsableId() != null) {
            Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Responsable no encontrado"));
            caso.setResponsable(responsable);
        } else if (request.getResponsableId() == null && caso.getResponsable() != null) {
            caso.setResponsable(null);
        }

        if (request.getAgenteRecibeId() != null) {
            Usuario agenteRecibe = usuarioRepository.findById(request.getAgenteRecibeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agente que recibe no encontrado"));
            caso.setAgenteRecibe(agenteRecibe);
        }

        if (request.getAgenteResolutorId() != null) {
            Usuario agenteResolutor = usuarioRepository.findById(request.getAgenteResolutorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agente resolutor no encontrado"));
            caso.setAgenteResolutor(agenteResolutor);
        }

        if (request.getFechaSolicitud() != null) {
            caso.setFechaSolicitud(request.getFechaSolicitud());
        }
        if (request.getNumeroCasoInt() != null) {
            caso.setNumeroCasoInt(request.getNumeroCasoInt());
        }
        if (request.getNumeroCasoESolution() != null) {
            caso.setNumeroCasoESolution(request.getNumeroCasoESolution());
        }
        if (request.getDescripcionFalla() != null) {
            caso.setDescripcionFalla(request.getDescripcionFalla());
        }
        if (request.getFechaSolucion() != null) {
            caso.setFechaSolucion(request.getFechaSolucion());
        }
        if (request.getObservaciones() != null) {
            caso.setObservaciones(request.getObservaciones());
        }

        caso.setActualizadoPor(getUsuarioAutenticado());

        Caso actualizado = casoRepository.save(caso);

        // ═══════════════════════════════════════════════════════
        // 4. COMPARAR Y REGISTRAR CADA CAMBIO EN EL HISTORIAL
        // ═══════════════════════════════════════════════════════
        try {
            // Estado
            String estadoNuevo = actualizado.getEstado() != null ? actualizado.getEstado().getNombre() : null;
            if (!Objects.equals(estadoAnterior, estadoNuevo)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "estado", estadoAnterior, estadoNuevo, null);
            }

            // Servicio
            String servicioNuevo = actualizado.getServicio() != null ? actualizado.getServicio().getNombre() : null;
            if (!Objects.equals(servicioAnterior, servicioNuevo)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "servicio", servicioAnterior, servicioNuevo, null);
            }

            // Tipo de servicio
            String tipoServicioNuevo = actualizado.getTipoServicio() != null ? actualizado.getTipoServicio().getNombre() : null;
            if (!Objects.equals(tipoServicioAnterior, tipoServicioNuevo)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "tipoServicio", tipoServicioAnterior, tipoServicioNuevo, null);
            }

            // Sede
            String sedeNueva = actualizado.getSedePrincipal() != null ? actualizado.getSedePrincipal().getNombreSede() : null;
            if (!Objects.equals(sedeAnterior, sedeNueva)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "sede", sedeAnterior, sedeNueva, null);
            }

            // Descripción
            if (!Objects.equals(descripcionAnterior, actualizado.getDescripcionFalla())) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "descripción", descripcionAnterior, actualizado.getDescripcionFalla(), null);
            }

            // Observaciones
            if (!Objects.equals(observacionesAnterior, actualizado.getObservaciones())) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "observaciones", observacionesAnterior, actualizado.getObservaciones(), null);
            }

            // E-Solution
            if (!Objects.equals(esolutionAnterior, actualizado.getNumeroCasoESolution())) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "número E-Solution", esolutionAnterior, actualizado.getNumeroCasoESolution(), null);
            }

            // Número interno
            if (!Objects.equals(numeroIntAnterior, actualizado.getNumeroCasoInt())) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "número interno", numeroIntAnterior, actualizado.getNumeroCasoInt(), null);
            }

            // Responsable
            String responsableNuevo = actualizado.getResponsable() != null ? actualizado.getResponsable().getNombre() : null;
            if (!Objects.equals(responsableAnterior, responsableNuevo)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "responsable", responsableAnterior, responsableNuevo, null);
            }

            // Agente que recibe
            String agenteRecibeNuevo = actualizado.getAgenteRecibe() != null ? actualizado.getAgenteRecibe().getNombre() : null;
            if (!Objects.equals(agenteRecibeAnterior, agenteRecibeNuevo)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "agenteRecibe", agenteRecibeAnterior, agenteRecibeNuevo, null);
            }

            // Agente resolutor
            String agenteResolutorNuevo = actualizado.getAgenteResolutor() != null ? actualizado.getAgenteResolutor().getNombre() : null;
            if (!Objects.equals(agenteResolutorAnterior, agenteResolutorNuevo)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "agenteResolutor", agenteResolutorAnterior, agenteResolutorNuevo, null);
            }

        } catch (Exception e) {
            log.warn("No se pudo registrar historial al actualizar caso: {}", e.getMessage());
        }

        return casoMapper.toResponse(actualizado);
    }
}