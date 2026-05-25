package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.caso.CasoRequest;
import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.service.CasoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CasoController {

    private final CasoService casoService;

    @PostMapping
    public CasoResponse crearCaso(@Valid @RequestBody CasoRequest request) {
        return casoService.crearCaso(request);
    }

    @GetMapping
    public List<CasoResponse> listarCasos() {
        return casoService.listarCasos();
    }

    @GetMapping("/{id}")
    public CasoResponse obtenerCasoPorId(@PathVariable Long id) {
        return casoService.obtenerCasoPorId(id);
    }

    @PutMapping("/{id}")
    public CasoResponse actualizarCaso(@PathVariable Long id, @Valid @RequestBody CasoRequest request) {
        return casoService.actualizarCaso(id, request);
    }
}