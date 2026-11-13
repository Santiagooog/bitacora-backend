package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.dto.caso.SugerenciaCasoResponse;
import com.sistema.bitacora.entity.*;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.mapper.CasoMapper;
import com.sistema.bitacora.repository.*;
import com.sistema.bitacora.service.AnssService;
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
import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final AnssService anssService;
    private final CategoriaFallaRepository categoriaFallaRepository;
    private final CategoriaSolucionRepository categoriaSolucionRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final ActivoRepository activoRepository;

    private static final Map<String, List<String>> TRANSICIONES_VALIDAS = Map.of(
            "ABIERTO", List.of("EN PROCESO", "CANCELADO"),
            "EN PROCESO", List.of("DEVUELTO", "SOLUCIONADO", "ESCALADO"),
            "DEVUELTO", List.of("CERRADO"),
            "CANCELADO", List.of("CERRADO"),
            "ESCALADO", List.of("SOPORTE EN SITIO", "FABRICANTE", "ESPECIALISTAS", "SOLUCIONADO"),
            "SOPORTE EN SITIO", List.of("SOLUCIONADO"),
            "FABRICANTE", List.of("SOLUCIONADO"),
            "ESPECIALISTAS", List.of("SOLUCIONADO"),
            "SOLUCIONADO", List.of("CERRADO"),
            "CERRADO", List.of()
    );

    @Override
    public CasoResponse crearCaso(CasoRequest request) {


        if (casoRepository.existsByNumeroCasoInt(request.getNumeroCasoInt())) {
            throw new IllegalArgumentException("Ya existe un caso con ese número interno.");
        }

        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        validarAccesoServicio(servicio.getId());

        TipoServicio tipoServicio = tipoServicioRepository.findById(request.getTipoServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado"));

        EstadoCaso estado;
        if (request.getEstadoId() != null) {
            estado = estadoCasoRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado"));
        } else {
            estado = estadoCasoRepository.findByNombre("ABIERTO")
                    .orElseThrow(() -> new ResourceNotFoundException("Estado ABIERTO no configurado"));
        }

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

        // Validar categoría de falla siempre obligatoria
        if (request.getCategoriaFallaId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría de falla");
        }

// Validar categoría de solución solo si estado es SOLUCIONADO
        if (estado.getNombre().equalsIgnoreCase("SOLUCIONADO") && request.getCategoriaSolucionId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría de solución para casos solucionados");
        }

        // Cargar entidad de falla siempre
        CategoriaFalla falla = categoriaFallaRepository.findById(request.getCategoriaFallaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de falla no encontrada"));

// Cargar solución solo si estado es SOLUCIONADO
        CategoriaSolucion solucion = null;
        if (estado.getNombre().equalsIgnoreCase("SOLUCIONADO")) {
            solucion = categoriaSolucionRepository.findById(request.getCategoriaSolucionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría de solución no encontrada"));
        }
        Activo activo = null;
        if (request.getActivoId() != null) {
            activo = activoRepository.findById(request.getActivoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado"));
        }

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
                .categoriaFalla(falla)
                .categoriaSolucion(solucion)
                .equipo(request.getEquipo())
                .modelo(request.getModelo())
                .serial(request.getSerial())
                .placa(request.getPlaca())
                .activo(activo)
                .proyecto(activo != null ? activo.getProyecto() : null)
                .canalEscalamiento(request.getCanalEscalamiento())
                .grupoEscalamiento(request.getGrupoEscalamiento()) // ✅ CORRECTO
                .escaladoA(request.getEscaladoA())
                .build();

        LocalDateTime fechaVencimiento = anssService.calcularVencimiento(
                request.getFechaSolicitud(),
                request.getServicioId(),
                request.getSedeId()
        );

        caso.setFechaVencimiento(fechaVencimiento);
        Usuario usuarioActual = getUsuarioAutenticado();
        caso.setCreadoPor(usuarioActual);

        Caso guardado = casoRepository.save(caso);

        try {
            historialService.registrar(
                    guardado.getId(),
                    "CREAR",
                    null, null, null,
                    "Caso creado con número " + guardado.getNumeroCasoInt()
            );
        } catch (Exception e) {
            log.warn("No se pudo registrar historial al crear caso: {}", e.getMessage());
        }

        return casoMapper.toResponse(guardado);
    }

    @Override
    public List<CasoResponse> listarCasos() {
        List<Long> permisos = obtenerServiciosPermitidosIds();
        return casoRepository.findAll()
                .stream()
                .filter(caso -> permisos == null || permisos.contains(caso.getServicio().getId()))
                .map(this::mapearConSaldoAns)
                .toList();
    }

    @Override
    public List<Long> obtenerServiciosPermitidosIds() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return List.of();
        if ("ADMIN".equalsIgnoreCase(usuario.getRol().getNombre())) return null;
        return usuario.getServiciosPermitidos().stream().map(Servicio::getId).toList();
    }

    private void validarAccesoServicio(Long servicioId) {
        List<Long> permisos = obtenerServiciosPermitidosIds();
        if (permisos != null && !permisos.contains(servicioId)) {
            throw new AccessDeniedException("No tienes acceso a este proyecto");
        }
    }

    private void validarAccesoCaso(Caso caso) {
        validarAccesoServicio(caso.getServicio().getId());
    }

    private CasoResponse mapearConSaldoAns(Caso caso) {
        CasoResponse respuesta = casoMapper.toResponse(caso);
        respuesta.setSegundosAnsRestantes(anssService.calcularSegundosRestantes(caso, LocalDateTime.now()));
        return respuesta;
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
    public List<SugerenciaCasoResponse> obtenerSugerencias(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        List<Long> permisos = obtenerServiciosPermitidosIds();
        return casoRepository.buscarCasosSimilares(texto.trim())
                .stream()
                .filter(caso -> permisos == null || permisos.contains(caso.getServicio().getId()))
                .limit(5)
                .map(this::toSugerencia)
                .toList();
    }

    @Override
    public Page<CasoResponse> listarCasosFiltrados(String termino, Long estadoId, Long servicioId, Long sedeId, LocalDateTime fechaDesde, LocalDateTime fechaHasta, boolean soloActivos, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("fechaSolicitud").descending());
        Page<Caso> casosPage;

        List<Long> permisos = obtenerServiciosPermitidosIds();
        if (permisos != null && permisos.isEmpty()) return Page.empty(pageable);
        if (soloActivos && estadoId == null) {
            // Si solo activos y no hay estado específico, filtrar por ABIERTO y ESCALADO
            casosPage = permisos == null
                    ? casoRepository.buscarCasosActivosConFiltros(termino, servicioId, sedeId, fechaDesde, fechaHasta, pageable)
                    : casoRepository.buscarCasosActivosParaServicios(termino, servicioId, sedeId, fechaDesde, fechaHasta, permisos, pageable);
        } else {
            // Filtrar normalmente (con o sin estado específico)
            casosPage = permisos == null
                    ? casoRepository.buscarCasosConFiltros(termino, estadoId, servicioId, sedeId, fechaDesde, fechaHasta, pageable)
                    : casoRepository.buscarCasosConFiltrosParaServicios(termino, estadoId, servicioId, sedeId, fechaDesde, fechaHasta, permisos, pageable);
        }

        return casosPage.map(this::mapearConSaldoAns);
    }

    private SugerenciaCasoResponse toSugerencia(Caso caso) {
        // Obtener la última observación del historial
        String ultimaObservacion = historialCasoRepository
                .findByCasoIdOrderByFechaDesc(caso.getId())
                .stream()
                .findFirst()
                .map(HistorialCaso::getObservacion)
                .orElse(caso.getObservaciones());

        return SugerenciaCasoResponse.builder()
                .id(caso.getId())
                .numeroCasoInt(caso.getNumeroCasoInt())
                .descripcionFalla(caso.getDescripcionFalla())
                .observaciones(ultimaObservacion != null ? ultimaObservacion : caso.getObservaciones())
                .estado(caso.getEstado() != null ? caso.getEstado().getNombre() : null)
                .servicio(caso.getServicio() != null ? caso.getServicio().getNombre() : null)
                .sede(caso.getSedePrincipal() != null ? caso.getSedePrincipal().getNombreSede() : null)
                .fechaSolucion(caso.getFechaSolucion())
                .categoriaFallaNombre(caso.getCategoriaFalla() != null ? caso.getCategoriaFalla().getNombre() : null)
                .categoriaSolucionNombre(caso.getCategoriaSolucion() != null ? caso.getCategoriaSolucion().getNombre() : null)
                .build();
    }

    @Override
    public List<CasoResponse> buscarCasosPorSede(String termino) {
        List<Long> permisos = obtenerServiciosPermitidosIds();
        return casoRepository.buscarPorSede(termino)
                .stream()
                .filter(caso -> permisos == null || permisos.contains(caso.getServicio().getId()))
                .map(this::mapearConSaldoAns)
                .toList();
    }

    @Override
    public Page<CasoResponse> listarCasosFiltrados(String termino, Long estadoId, Long servicioId, Long sedeId, LocalDateTime fechaDesde, LocalDateTime fechaHasta, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("fechaSolicitud").descending());
        List<Long> permisos = obtenerServiciosPermitidosIds();
        if (permisos != null && permisos.isEmpty()) return Page.empty(pageable);
        Page<Caso> casosPage = permisos == null
                ? casoRepository.buscarCasosConFiltros(termino, estadoId, servicioId, sedeId, fechaDesde, fechaHasta, pageable)
                : casoRepository.buscarCasosConFiltrosParaServicios(termino, estadoId, servicioId, sedeId, fechaDesde, fechaHasta, permisos, pageable);
        return casosPage.map(this::mapearConSaldoAns);
    }

    @Override
    public String generarNumeroCasoAutomatico(String nombreServicio, String tipo) {
        servicioRepository.findByNombreIgnoreCase(nombreServicio)
                .ifPresentOrElse(servicio -> validarAccesoServicio(servicio.getId()),
                        () -> { throw new ResourceNotFoundException("Servicio no encontrado"); });
        String prefijoServicio = "";
        String nombreServicioUpper = nombreServicio.toUpperCase();

        if (nombreServicioUpper.contains("SDWAN")) {
            prefijoServicio = "SDWN";
        } else if (nombreServicioUpper.contains("WIFI PUBLICO") || nombreServicioUpper.contains("WIFI PÚBLICO")) {
            prefijoServicio = "WPB";
        } else if (nombreServicioUpper.contains("CAS 1528A")) {
            prefijoServicio = "CAS1528A";
        } else if (nombreServicioUpper.contains("CAS 1528")) {
            prefijoServicio = "CAS1528";
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
        validarAccesoCaso(caso);

        // Primero eliminar el historial asociado
        historialCasoRepository.deleteByCasoId(id);

        // Luego eliminar el caso
        casoRepository.delete(caso);
    }

    @Override
    public CasoResponse obtenerCasoPorId(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        validarAccesoCaso(caso);
        return mapearConSaldoAns(caso);
    }

    @Override
    public Page<CasoResponse> listarCasosPaginado(String termino, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("fechaSolicitud").descending());
        List<Long> permisos = obtenerServiciosPermitidosIds();
        if (permisos != null && permisos.isEmpty()) return Page.empty(pageable);
        Page<Caso> casosPage = permisos == null ? casoRepository.buscarCasosPaginado(termino, pageable)
                : casoRepository.buscarCasosPaginadoParaServicios(termino, permisos, pageable);
        return casosPage.map(this::mapearConSaldoAns);
    }

    @Override
    @Transactional
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
        String grupoEscalamientoAnterior = caso.getGrupoEscalamiento();
        String escaladoAAnterior = caso.getEscaladoA();
        String categoriaSolucionAnterior = caso.getCategoriaSolucion() != null
                ? caso.getCategoriaSolucion().getNombre() : null;
        String serialActivoAnterior = null;
        String placaActivoAnterior = null;
        String serialActivoNuevo = null;
        String placaActivoNueva = null;

        // ═══════════════════════════════════════════════════════
        // 2. VALIDAR CATEGORÍAS
        // ═══════════════════════════════════════════════════════
        // Categoría de falla siempre obligatoria
        if (request.getCategoriaFallaId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría de falla");
        }

        // Determinar si el estado final será SOLUCIONADO
        // Determinar si el estado final será SOLUCIONADO
        boolean esSolucionado = false;

        if (request.getEstadoId() != null) {
            EstadoCaso nuevoEstado = estadoCasoRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado"));

            String estadoActualNombre = caso.getEstado() != null ? caso.getEstado().getNombre() : null;
            String nuevoEstadoNombre = nuevoEstado.getNombre();

            // Validar que el caso no esté cerrado
            if ("CERRADO".equalsIgnoreCase(estadoActualNombre)) {
                throw new IllegalArgumentException(
                        "El caso está cerrado y no puede modificarse. Cree un caso nuevo si necesita hacer cambios."
                );
            }

            // Validar transición si cambia el estado
            if (estadoActualNombre != null && !estadoActualNombre.equals(nuevoEstadoNombre)) {
                List<String> permitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoActualNombre, List.of());
                if (!permitidos.contains(nuevoEstadoNombre)) {
                    throw new IllegalArgumentException(
                            "Transición no permitida: de '" + estadoActualNombre + "' a '" + nuevoEstadoNombre + "'"
                    );
                }
            }

            // ✅ Asignar el nuevo estado al caso
            caso.setEstado(nuevoEstado);

            // ✅ Determinar si el estado final es SOLUCIONADO
            esSolucionado = "SOLUCIONADO".equalsIgnoreCase(nuevoEstadoNombre);

        } else {
            // Si no se envía estado, usar el actual del caso
            esSolucionado = caso.getEstado() != null
                    && caso.getEstado().getNombre().equalsIgnoreCase("SOLUCIONADO");
        }

        // Validar categoría de solución según estado
        if (esSolucionado && request.getCategoriaSolucionId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría de solución para casos solucionados");
        }

        if (!esSolucionado && request.getCategoriaSolucionId() != null) {
            throw new IllegalArgumentException("No puede asignar solución a un caso no solucionado");
        }

        // Cargar categoría de falla
        CategoriaFalla falla = categoriaFallaRepository.findById(request.getCategoriaFallaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de falla no encontrada"));

        // Cargar categoría de solución solo si es SOLUCIONADO
        CategoriaSolucion solucion = null;
        if (esSolucionado) {
            solucion = categoriaSolucionRepository.findById(request.getCategoriaSolucionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría de solución no encontrada"));
        }

        // ═══════════════════════════════════════════════════════
        // 3. APLICAR LAS MODIFICACIONES
        // ═══════════════════════════════════════════════════════
        if (request.getServicioId() != null) {
            validarAccesoServicio(request.getServicioId());
            Servicio servicio = servicioRepository.findById(request.getServicioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
            caso.setServicio(servicio);
        }

        if (request.getTipoServicioId() != null) {
            TipoServicio tipoServicio = tipoServicioRepository.findById(request.getTipoServicioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de servicio no encontrado"));
            caso.setTipoServicio(tipoServicio);
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
        if (request.getActivoId() != null) {
            Activo activo = activoRepository.findById(request.getActivoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado"));
            caso.setActivo(activo);
            caso.setProyecto(activo.getProyecto());
        }
        if (request.getCanalEscalamiento() != null) {
            caso.setCanalEscalamiento(request.getCanalEscalamiento());
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
        if (request.getEquipo() != null) {
            caso.setEquipo(request.getEquipo());
        }
        if (request.getModelo() != null) {
            caso.setModelo(request.getModelo());
        }
        if (request.getSerial() != null) {
            caso.setSerial(request.getSerial());
        }
        if (request.getPlaca() != null) {
            caso.setPlaca(request.getPlaca());
        }

        // ✅ Guardar los campos de escalamiento
        if (request.getGrupoEscalamiento() != null) {
            caso.setGrupoEscalamiento(request.getGrupoEscalamiento());
        }
        if (request.getEscaladoA() != null) {
            caso.setEscaladoA(request.getEscaladoA());
        }

        // ✅ Setear categorías
        caso.setCategoriaFalla(falla);
        caso.setCategoriaSolucion(solucion);

        // Para las soluciones de cambio, reemplazar serial y placa del activo vinculado al caso.
        if (esSolucionado && solucion != null && esCategoriaCambioActivo(solucion.getNombre())) {
            Activo activo = caso.getActivo();
            if (activo == null) {
                throw new IllegalArgumentException("Debe asociar un activo al caso para registrar su reemplazo");
            }
            if (request.getSerialNuevo() == null || request.getSerialNuevo().isBlank()) {
                throw new IllegalArgumentException("El nuevo serial es obligatorio para registrar el cambio de equipo");
            }
            if (request.getPlacaNueva() == null || request.getPlacaNueva().isBlank()) {
                throw new IllegalArgumentException("La nueva placa es obligatoria para registrar el cambio de equipo");
            }

            serialActivoAnterior = activo.getSerial() != null ? activo.getSerial() : caso.getSerial();
            placaActivoAnterior = activo.getPlaca() != null ? activo.getPlaca() : caso.getPlaca();

            String serialNuevo = request.getSerialNuevo().trim();
            activoRepository.findBySerial(serialNuevo)
                    .filter(existente -> !existente.getId().equals(activo.getId()))
                    .ifPresent(existente -> {
                        throw new IllegalArgumentException("Ya existe un activo con el serial " + serialNuevo);
                    });

            serialActivoNuevo = serialNuevo;
            placaActivoNueva = request.getPlacaNueva().trim();
            activo.setSerial(serialActivoNuevo);
            activo.setPlaca(placaActivoNueva);
            activoRepository.save(activo);
        }

        // ✅ Recalcular fecha de vencimiento si cambió fecha/servicio/sede
        if (request.getFechaSolicitud() != null || request.getServicioId() != null || request.getSedeId() != null) {
            LocalDateTime fechaVencimiento = anssService.calcularVencimiento(
                    caso.getFechaSolicitud(),
                    caso.getServicio().getId(),
                    caso.getSedePrincipal().getId()
            );
            caso.setFechaVencimiento(fechaVencimiento);
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

            // Categoría de solución
            String categoriaSolucionNueva = actualizado.getCategoriaSolucion() != null
                    ? actualizado.getCategoriaSolucion().getNombre() : null;
            if (!Objects.equals(categoriaSolucionAnterior, categoriaSolucionNueva)) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "categoría de solución",
                        categoriaSolucionAnterior, categoriaSolucionNueva, null);
            }

            // Trazabilidad del reemplazo; los datos previos quedan en el historial aunque el activo se actualice.
            if (esCategoriaCambioActivo(categoriaSolucionNueva)) {
                if (!Objects.equals(serialActivoAnterior, serialActivoNuevo)) {
                    historialService.registrar(actualizado.getId(), "ACTUALIZAR", "serial del activo",
                            serialActivoAnterior, serialActivoNuevo, "Reemplazo de equipo");
                }
                if (!Objects.equals(placaActivoAnterior, placaActivoNueva)) {
                    historialService.registrar(actualizado.getId(), "ACTUALIZAR", "placa del activo",
                            placaActivoAnterior, placaActivoNueva, "Reemplazo de equipo");
                }
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

            // Grupo de Escalamiento
            if (!Objects.equals(grupoEscalamientoAnterior, actualizado.getGrupoEscalamiento())) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "grupo de escalamiento", grupoEscalamientoAnterior, actualizado.getGrupoEscalamiento(), null);
            }

            // Escalado a (Aliado)
            if (!Objects.equals(escaladoAAnterior, actualizado.getEscaladoA())) {
                historialService.registrar(actualizado.getId(), "ACTUALIZAR", "escalado a", escaladoAAnterior, actualizado.getEscaladoA(), null);
            }


        } catch (Exception e) {
            log.warn("No se pudo registrar historial al actualizar caso: {}", e.getMessage());
        }

        return casoMapper.toResponse(actualizado);
    }

    private boolean esCategoriaCambioActivo(String nombre) {
        if (nombre == null) return false;
        String normalizado = nombre.trim().toLowerCase().replaceAll("\\s+", " ");
        return List.of(
                "cambio de equipo", "cambio de sw", "cambio de fw", "cambio de usg",
                "cambio de ar", "cambio de ap", "cambio de poe", "ar dañado", "ap dañado"
        )
                .contains(normalizado);
    }
}
