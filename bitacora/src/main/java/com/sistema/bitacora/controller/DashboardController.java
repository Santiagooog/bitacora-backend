package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.caso.DashboardResponse;
import com.sistema.bitacora.dto.caso.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> obtenerEstadisticas(
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long estadoId,
            @RequestParam(required = false, defaultValue = "") String termino,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta) {
        LocalDateTime desde = fechaDesde == null || fechaDesde.isBlank()
                ? null : LocalDateTime.parse(fechaDesde + "T00:00:00");
        LocalDateTime hasta = fechaHasta == null || fechaHasta.isBlank()
                ? null : LocalDateTime.parse(fechaHasta + "T23:59:59");
        return ResponseEntity.ok(dashboardService.obtenerEstadisticas(
                servicioId, estadoId, termino, desde, hasta));
    }
}
