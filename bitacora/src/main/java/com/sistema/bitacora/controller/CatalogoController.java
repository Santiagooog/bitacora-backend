package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.catalogo.CatalogoResponse;
import com.sistema.bitacora.dto.catalogo.SedeCompletaResponse;
import com.sistema.bitacora.entity.Rol;
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

    @GetMapping("/roles")
    public List<Rol> listarRoles() {
        return catalogoService.listarRoles();
    }

    @GetMapping("/sedes")
    public List<SedeCompletaResponse> listarSedes() {
        return catalogoService.listarSedes();
    }

    // ✅ NUEVO endpoint para búsqueda por SBAN o nombre
    @GetMapping("/sedes/buscar")
    public List<SedeCompletaResponse> buscarSedes(@RequestParam String termino) {
        return catalogoService.buscarSedes(termino);
    }
}