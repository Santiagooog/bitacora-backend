package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.catalogo.CatalogoResponse;
import com.sistema.bitacora.dto.catalogo.CrearServicioRequest;
import com.sistema.bitacora.dto.catalogo.SedeCompletaResponse;
import com.sistema.bitacora.entity.Rol;
import com.sistema.bitacora.entity.Servicio;
import com.sistema.bitacora.repository.ActivoRepository;
import com.sistema.bitacora.repository.ServicioRepository;
import com.sistema.bitacora.repository.UsuarioRepository;
import com.sistema.bitacora.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final ActivoRepository activoRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/servicios")
    @Transactional(readOnly = true)
    public List<CatalogoResponse> listarServicios(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return catalogoService.listarServicios();
        }
        return usuarioRepository.findByCorreo(authentication.getName())
                .map(usuario -> usuario.getServiciosPermitidos().stream()
                        .filter(servicio -> Boolean.TRUE.equals(servicio.getActivo()))
                        .map(servicio -> new CatalogoResponse(servicio.getId(), servicio.getNombre()))
                        .sorted(java.util.Comparator.comparing(CatalogoResponse::getNombre))
                        .toList())
                .orElse(List.of());
    }

    @PostMapping("/servicios")
    public CatalogoResponse crearServicio(@RequestBody CrearServicioRequest request) {
        return catalogoService.crearServicio(request.getNombre(), request.getDescripcion());
    }

    @GetMapping("/tipos-servicio")
    public List<CatalogoResponse> listarTiposServicio() {
        return catalogoService.listarTiposServicio();
    }

    @GetMapping("/estados")
    public List<CatalogoResponse> listarEstados() {
        return catalogoService.listarEstados();
    }

    @GetMapping("/roles")
    public List<Rol> listarRoles() {
        return catalogoService.listarRoles();
    }

    @GetMapping("/sedes")
    public List<SedeCompletaResponse> listarSedes() {
        return catalogoService.listarSedes();
    }

    // ✅ NUEVO endpoint para búsqueda por SBAN o nombre
    @GetMapping("/sedes/buscar")
    public List<SedeCompletaResponse> buscarSedes(@RequestParam String termino) {
        return catalogoService.buscarSedes(termino);
    }

    @GetMapping("/sedes/buscar-por-servicio")
    public List<SedeCompletaResponse> buscarSedesPorServicio(
            @RequestParam(required = false, defaultValue = "") String termino,
            @RequestParam Long servicioId, Authentication authentication) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            boolean permitido = authentication != null && usuarioRepository.findByCorreo(authentication.getName())
                    .map(usuario -> usuario.getServiciosPermitidos().stream().anyMatch(servicioPermitido -> servicioPermitido.getId().equals(servicioId)))
                    .orElse(false);
            if (!permitido) throw new AccessDeniedException("No tienes acceso a este proyecto");
        }

        String nombre = servicio.getNombre().toUpperCase();
        Set<String> tiposServicioSet = new HashSet<>(List.of(servicio.getNombre()));
        if (nombre.contains("CAS 1528A")) tiposServicioSet.add("CAS_1528A");
        else if (nombre.contains("CAS 1528")) tiposServicioSet.add("CAS_1528");
        if (nombre.contains("WIFI")) tiposServicioSet.add("WIFI");
        if (nombre.contains("SDWAN")) tiposServicioSet.add("SDWAN");
        if (nombre.contains("CONTROL DE ACCESO")) tiposServicioSet.add("ACCESOS");
        List<String> tiposServicio = List.copyOf(tiposServicioSet);

        Set<String> sbansConServicio = new HashSet<>(
                activoRepository.findSbansByTipoServicioInAndActivoTrue(tiposServicio));
        List<SedeCompletaResponse> sedes = termino.isBlank()
                ? catalogoService.listarSedes()
                : catalogoService.buscarSedes(termino);

        return sedes.stream()
                .filter(sede -> Boolean.TRUE.equals(sede.getActiva()))
                .filter(sede -> sbansConServicio.contains(sede.getSban()))
                .toList();
    }}
