package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.puntoAcceso.CargaMasivaPuntoResponse;
import com.sistema.bitacora.entity.PuntoAcceso;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.repository.PuntoAccesoRepository;
import com.sistema.bitacora.service.PuntoAccesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/puntos-acceso")
@RequiredArgsConstructor
public class PuntoAccesoController {

    private final PuntoAccesoRepository repository;
    private final PuntoAccesoService service;

    // ✅ Listado paginado con filtros
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PuntoAcceso>> listarTodos(
            @RequestParam(defaultValue = "") String termino,
            @RequestParam(required = false) String sban,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String regional,
            @RequestParam(required = false) String proyecto,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("sban").ascending().and(Sort.by("placa")));
        Page<PuntoAcceso> resultado = repository.buscarConFiltros(
                termino == null ? "" : termino.trim(), vacioANull(sban), vacioANull(tipo), vacioANull(regional), vacioANull(proyecto), pageable);

        return ResponseEntity.ok(resultado);
    }

    // ✅ Listar todos los dispositivos de un punto de acceso por PLACA
    @GetMapping("/por-placa/{placa}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PuntoAcceso>> porPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(repository.findByPlaca(placa));
    }

    // ✅ Conteo por sede
    @GetMapping("/conteo-por-sede")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> conteoPorSede() {
        List<Map<String, Object>> resultado = repository.contarPorSban()
                .stream()
                .map(row -> Map.of(
                        "sban", row[0] != null ? row[0] : "",
                        "total", ((Number) row[1]).longValue()
                ))
                .toList();
        return ResponseEntity.ok(resultado);
    }

    // ✅ Dropdowns para filtros
    @GetMapping("/sbans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listarSbans() {
        return ResponseEntity.ok(repository.findDistinctSbans());
    }

    @GetMapping("/regionales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listarRegionales() {
        return ResponseEntity.ok(repository.findDistinctRegionales());
    }

    @GetMapping("/tipos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listarTipos() {
        return ResponseEntity.ok(repository.findDistinctTipos());
    }

    @GetMapping("/proyectos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listarProyectos() {
        return ResponseEntity.ok(List.of("CAS 1528", "CAS 1528A"));
    }

    // ✅ Eliminar
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Punto de acceso no encontrado");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Sincronizar puntos de acceso existentes a la tabla activos
    @PostMapping("/sincronizar-a-activos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sincronizarAActivos() {
        int sincronizados = service.sincronizarAActivos();
        return ResponseEntity.ok(Map.of(
                "sincronizados", sincronizados,
                "mensaje", sincronizados + " puntos de acceso sincronizados a la tabla de activos"
        ));
    }

    private String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    // ✅ Carga masiva desde .csv o .txt
    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CargaMasivaPuntoResponse> cargaMasiva(
            @RequestParam("archivo") MultipartFile archivo) throws IOException {

        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null) {
            throw new IllegalArgumentException("El archivo no tiene nombre");
        }
        String lower = nombre.toLowerCase();
        if (!lower.endsWith(".txt") && !lower.endsWith(".csv")) {
            throw new IllegalArgumentException("Solo se aceptan archivos .txt o .csv");
        }

        return ResponseEntity.ok(service.procesarCsv(archivo.getInputStream()));
    }
}
