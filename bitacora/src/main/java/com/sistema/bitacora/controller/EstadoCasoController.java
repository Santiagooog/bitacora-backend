package com.sistema.bitacora.controller;

import com.sistema.bitacora.entity.EstadoCaso;
import com.sistema.bitacora.repository.EstadoCasoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoCasoController {

    private final EstadoCasoRepository estadoCasoRepository;

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

    @GetMapping("/transiciones/{estadoActual}")
    public ResponseEntity<List<EstadoCaso>> obtenerTransiciones(@PathVariable String estadoActual) {
        List<String> permitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoActual, List.of());
        return ResponseEntity.ok(estadoCasoRepository.findByNombreIn(permitidos));
    }
}