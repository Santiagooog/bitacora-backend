package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.historial.HistorialCasoResponse;
import com.sistema.bitacora.service.HistorialCasoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
public class HistorialCasoController {

    private final HistorialCasoService historialService;

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialCasoResponse>> obtenerHistorial(@PathVariable Long id) {
        return ResponseEntity.ok(historialService.listarPorCaso(id));
    }
}