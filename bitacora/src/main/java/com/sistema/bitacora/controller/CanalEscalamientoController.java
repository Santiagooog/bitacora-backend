package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.canal.CanalEscalamientoResponse;
import com.sistema.bitacora.service.CanalEscalamientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canales-escalamiento")
@RequiredArgsConstructor
public class CanalEscalamientoController {

    private final CanalEscalamientoService canalService;

    @GetMapping
    public ResponseEntity<List<CanalEscalamientoResponse>> listar() {
        return ResponseEntity.ok(canalService.listarActivos());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CanalEscalamientoResponse>> listarTodos() {
        return ResponseEntity.ok(canalService.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CanalEscalamientoResponse> crear(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion) {
        return ResponseEntity.ok(canalService.crear(nombre, descripcion));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CanalEscalamientoResponse> actualizar(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(canalService.actualizar(id, nombre, descripcion, activo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        canalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}