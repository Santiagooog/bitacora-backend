package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.dto.caso.SugerenciaCasoResponse;
import com.sistema.bitacora.service.CasoService;
import com.sistema.bitacora.service.CasoExcelStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
@Slf4j
public class CasoController {

    private final CasoService casoService;
    private final CasoExcelStreamingService casoExcelStreamingService;

    @GetMapping
    public ResponseEntity<List<CasoResponse>> listar() {
        return ResponseEntity.ok(casoService.listarCasos());
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<SugerenciaCasoResponse>> sugerencias(@RequestParam String texto) {
        return ResponseEntity.ok(casoService.obtenerSugerencias(texto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CasoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(casoService.obtenerCasoPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')") // ✅
    public ResponseEntity<CasoResponse> crear(@RequestBody CasoRequest request) {
        return ResponseEntity.ok(casoService.crearCaso(request));
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<CasoResponse>> listarPaginado(
            @RequestParam(defaultValue = "") String termino,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return ResponseEntity.ok(casoService.listarCasosPaginado(termino, pagina, tamanio));
    }

    @GetMapping("/filtrados")
    public ResponseEntity<Page<CasoResponse>> listarFiltrados(
            @RequestParam(defaultValue = "") String termino,
            @RequestParam(required = false) Long estadoId,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long sedeId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(defaultValue = "true") boolean soloActivos,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {

        LocalDateTime desde = null;
        LocalDateTime hasta = null;

        if (fechaDesde != null && !fechaDesde.isBlank()) {
            desde = LocalDateTime.parse(fechaDesde + "T00:00:00");
        }
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            hasta = LocalDateTime.parse(fechaHasta + "T23:59:59");
        }

        return ResponseEntity.ok(casoService.listarCasosFiltrados(termino, estadoId, servicioId, sedeId, desde, hasta, soloActivos, pagina, tamanio));
    }

    @GetMapping(value = "/exportar-excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<StreamingResponseBody> exportarExcel(
            @RequestParam(defaultValue = "") String termino,
            @RequestParam(required = false) Long estadoId,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long sedeId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        LocalDateTime desde = fechaDesde == null || fechaDesde.isBlank()
                ? null : LocalDateTime.parse(fechaDesde + "T00:00:00");
        LocalDateTime hasta = fechaHasta == null || fechaHasta.isBlank()
                ? null : LocalDateTime.parse(fechaHasta + "T23:59:59");

        List<Long> serviciosPermitidos = casoService.obtenerServiciosPermitidosIds();
        StreamingResponseBody stream = salida -> {
            try {
                casoExcelStreamingService.escribirExcel(
                        termino, estadoId, servicioId, sedeId, desde, hasta, soloActivos,
                        serviciosPermitidos, salida);
            } catch (IOException | RuntimeException error) {
                log.error("Falló la generación del Excel de casos durante la transmisión", error);
                throw error;
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"casos_trazabilidad.xlsx\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(stream);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')") // ✅
    public ResponseEntity<CasoResponse> actualizar(@PathVariable Long id, @RequestBody CasoRequest request) {
        return ResponseEntity.ok(casoService.actualizarCaso(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')") // ✅
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        casoService.eliminarCaso(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CasoResponse>> buscar(@RequestParam String termino) {
        return ResponseEntity.ok(casoService.buscarCasosPorSede(termino));
    }

    @GetMapping("/generar-numero")
    public ResponseEntity<String> generarNumero(
            @RequestParam String servicio,
            @RequestParam String tipo) {
        return ResponseEntity.ok(casoService.generarNumeroCasoAutomatico(servicio, tipo));
    }
}
