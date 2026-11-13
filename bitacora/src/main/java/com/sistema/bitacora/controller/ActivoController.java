package com.sistema.bitacora.controller;

import com.sistema.bitacora.entity.Activo;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.repository.ActivoRepository;
import com.sistema.bitacora.repository.ServicioRepository;
import com.sistema.bitacora.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sistema.bitacora.dto.activo.CargaMasivaResponse;
import com.sistema.bitacora.service.ActivoService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/activos")
@RequiredArgsConstructor
public class ActivoController {

    private final ActivoRepository activoRepository;
    private final ActivoService activoService;   // ← ✅ NUEVO
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    // ✅ Listar activos por sede y tipo de servicio
    @GetMapping("/por-sede")
    public ResponseEntity<List<Activo>> porSede(
            @RequestParam String sban,
            @RequestParam(required = false) String tipoServicio, Authentication authentication) {
        List<Activo> activos;
        if (tipoServicio != null && !tipoServicio.isBlank()) {
            boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMIN"));
            if (!esAdmin) {
                boolean permitido = authentication != null && usuarioRepository.findByCorreo(authentication.getName())
                        .map(usuario -> usuario.getServiciosPermitidos().stream()
                                .anyMatch(servicio -> coincideServicio(servicio.getNombre(), tipoServicio)))
                        .orElse(false);
                if (!permitido) throw new AccessDeniedException("No tienes acceso a los activos de este proyecto");
            }
            activos = activoRepository.findBySbanAndTipoServicio(sban, tipoServicio);
        } else {
            activos = activoRepository.findBySban(sban);
        }
        return ResponseEntity.ok(activos);
    }

    private boolean coincideServicio(String nombreServicio, String tipoSolicitado) {
        String nombre = nombreServicio.toUpperCase(java.util.Locale.ROOT);
        String tipo = tipoSolicitado.toUpperCase(java.util.Locale.ROOT);
        if (tipo.equals("WIFI")) return nombre.contains("WIFI");
        if (tipo.equals("SDWAN")) return nombre.contains("SDWAN");
        if (tipo.equals("CAS_1528A")) return nombre.contains("CAS 1528A");
        if (tipo.equals("CAS_1528")) return nombre.contains("CAS 1528") && !nombre.contains("1528A");
        if (tipo.equals("ACCESOS")) return nombre.contains("CONTROL DE ACCESO") || nombre.contains("CAS 1528");
        return nombre.equals(tipo);
    }

    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CargaMasivaResponse> cargaMasiva(

            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoServicio") String tipoServicio) throws IOException {

        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String nombre = archivo.getOriginalFilename();
        if (nombre == null) {
            throw new IllegalArgumentException("El archivo no tiene nombre");
        }

        String nombreLower = nombre.toLowerCase();
        if (!nombreLower.endsWith(".txt") && !nombreLower.endsWith(".csv")) {
            throw new IllegalArgumentException("Solo se aceptan archivos .txt o .csv");
        }

// En tu ActivoController, método cargaMasiva:
        List<String> tiposLegados = List.of("WIFI", "SDWAN", "CAS_1528", "CAS_1528A", "ACCESOS");
        if (!tiposLegados.contains(tipoServicio) && servicioRepository.findByNombreIgnoreCase(tipoServicio).isEmpty()) {
            throw new IllegalArgumentException("Tipo de servicio inválido: " + tipoServicio);
        }

        return ResponseEntity.ok(activoService.procesarCsv(archivo.getInputStream(), tipoServicio));
    }

    // ✅ Listar con paginación y búsqueda (solo admin)
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Activo>> listarTodos(
            @RequestParam(defaultValue = "") String termino,
            @RequestParam(required = false) String tipoServicio,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("sban").ascending());
        Page<Activo> resultado;

        boolean hayTipo = tipoServicio != null && !tipoServicio.isBlank();
        boolean hayTermino = !termino.isBlank();

        if (hayTipo && hayTermino) {
            resultado = activoRepository.buscarPorTipoYTermino(tipoServicio, termino, pageable);
        } else if (hayTipo) {
            resultado = activoRepository.findByTipoServicio(tipoServicio, pageable);
        } else if (hayTermino) {
            resultado = activoRepository.buscarPorTermino(termino, pageable);
        } else {
            resultado = activoRepository.findAll(pageable);
        }

        return ResponseEntity.ok(resultado);
    }

    // ✅ Crear activo (solo admin)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Activo> crear(@RequestBody Activo activo) {
        if (activo.getSerial() == null || activo.getSerial().isBlank()) {
            throw new IllegalArgumentException("El serial es obligatorio");
        }
        if (activoRepository.findBySerial(activo.getSerial()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un activo con ese serial");
        }
        activo.setId(null);
        if (activo.getActivo() == null) activo.setActivo(true);
        asignarProyectoCas(activo);
        return ResponseEntity.ok(activoRepository.save(activo));
    }

    // ✅ Actualizar activo (solo admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Activo> actualizar(@PathVariable Long id, @RequestBody Activo datos) {
        Activo activo = activoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado"));

        if (datos.getSerial() != null && !datos.getSerial().equals(activo.getSerial())) {
            if (activoRepository.findBySerial(datos.getSerial()).isPresent()) {
                throw new IllegalArgumentException("Ya existe otro activo con ese serial");
            }
            activo.setSerial(datos.getSerial());
        }

        if (datos.getName() != null) activo.setName(datos.getName());
        if (datos.getSban() != null) activo.setSban(datos.getSban());
        if (datos.getSite() != null) activo.setSite(datos.getSite());
        if (datos.getDeviceType() != null) activo.setDeviceType(datos.getDeviceType());
        if (datos.getPlaca() != null) activo.setPlaca(datos.getPlaca());
        if (datos.getTipoServicio() != null) activo.setTipoServicio(datos.getTipoServicio());
        if (datos.getActivo() != null) activo.setActivo(datos.getActivo());
        if (datos.getProyecto() != null) activo.setProyecto(datos.getProyecto());
        asignarProyectoCas(activo);

        return ResponseEntity.ok(activoRepository.save(activo));
    }

    private void asignarProyectoCas(Activo activo) {
        if ("CAS_1528".equalsIgnoreCase(activo.getTipoServicio())) {
            activo.setProyecto("CAS 1528");
        } else if ("CAS_1528A".equalsIgnoreCase(activo.getTipoServicio())) {
            activo.setProyecto("CAS 1528A");
        }
    }

    // ✅ Eliminar activo (solo admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!activoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Activo no encontrado");
        }
        activoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
