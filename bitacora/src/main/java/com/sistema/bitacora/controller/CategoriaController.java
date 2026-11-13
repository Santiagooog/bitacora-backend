package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.categoria.CategoriaFallaResponse;
import com.sistema.bitacora.dto.categoria.CategoriaSolucionResponse;
import com.sistema.bitacora.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    // ===== FALLAS =====
    @GetMapping("/fallas/servicio/{servicioId}")
    public ResponseEntity<List<CategoriaFallaResponse>> listarFallas(@PathVariable Long servicioId) {
        return ResponseEntity.ok(categoriaService.listarFallas(servicioId));
    }

    @PostMapping("/fallas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaFallaResponse> crearFalla(@RequestParam Long servicioId, @RequestParam String nombre) {
        return ResponseEntity.ok(categoriaService.crearFalla(servicioId, nombre));
    }

    @DeleteMapping("/fallas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarFalla(@PathVariable Long id) {
        categoriaService.eliminarFalla(id);
        return ResponseEntity.noContent().build();
    }

    // ===== SOLUCIONES =====
    @GetMapping("/soluciones/servicio/{servicioId}")
    public ResponseEntity<List<CategoriaSolucionResponse>> listarSoluciones(@PathVariable Long servicioId) {
        return ResponseEntity.ok(categoriaService.listarSoluciones(servicioId));
    }

    @PostMapping("/soluciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaSolucionResponse> crearSolucion(@RequestParam Long servicioId, @RequestParam String nombre) {
        return ResponseEntity.ok(categoriaService.crearSolucion(servicioId, nombre));
    }

    @DeleteMapping("/soluciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarSolucion(@PathVariable Long id) {
        categoriaService.eliminarSolucion(id);
        return ResponseEntity.noContent().build();
    }
}