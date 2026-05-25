package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.catalogo.CatalogoResponse;
import com.sistema.bitacora.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/servicios")
    public List<CatalogoResponse> listarServicios() {
        return catalogoService.listarServicios();
    }

    @GetMapping("/tipos-servicio")
    public List<CatalogoResponse> listarTiposServicio() {
        return catalogoService.listarTiposServicio();
    }

    @GetMapping("/estados")
    public List<CatalogoResponse> listarEstados() {
        return catalogoService.listarEstados();
    }

    @GetMapping("/sedes")
    public List<CatalogoResponse> listarSedes() {
        return catalogoService.listarSedes();
    }
}