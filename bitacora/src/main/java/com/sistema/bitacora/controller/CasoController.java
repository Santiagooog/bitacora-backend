package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.service.CasoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
public class CasoController {

    private final CasoService casoService;

    @GetMapping
    public ResponseEntity<List<CasoResponse>> listar() {
        return ResponseEntity.ok(casoService.listarCasos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CasoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(casoService.obtenerCasoPorId(id));
    }

    @PostMapping
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

    @PutMapping("/{id}")
    public ResponseEntity<CasoResponse> actualizar(@PathVariable Long id, @RequestBody CasoRequest request) {
        return ResponseEntity.ok(casoService.actualizarCaso(id, request));
    }

    @DeleteMapping("/{id}")
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