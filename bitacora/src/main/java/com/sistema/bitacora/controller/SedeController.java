package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.sede.CargaMasivaSedeResponse;
import com.sistema.bitacora.entity.Sede;
import com.sistema.bitacora.exception.ResourceNotFoundException;
import com.sistema.bitacora.repository.SedeRepository;
import com.sistema.bitacora.service.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final SedeRepository sedeRepository;
    private final SedeService sedeService;

    // ✅ Listar todas las regionales únicas (para el filtro del frontend)
    @GetMapping("/regionales")
    public ResponseEntity<List<String>> listarRegionales() {
        return ResponseEntity.ok(sedeRepository.findDistinctRegionales());
    }

    // ✅ Obtener sede por SBAN (existente)
    @GetMapping("/sban/{sban}")
    public ResponseEntity<Sede> obtenerSedePorSban(@PathVariable String sban) {
        return sedeRepository.findBySban(sban)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Listado paginado con filtros
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Sede>> listarTodos(
            @RequestParam(defaultValue = "") String termino,
            @RequestParam(required = false) String regional,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("sban").ascending());
        Page<Sede> resultado;

        boolean hayRegional = regional != null && !regional.isBlank();
        boolean hayTermino = !termino.isBlank();

        if (hayRegional && hayTermino) {
            resultado = sedeRepository.buscarPorRegionalYTermino(regional, termino, pageable);
        } else if (hayRegional) {
            resultado = sedeRepository.findByRegional(regional, pageable);
        } else if (hayTermino) {
            resultado = sedeRepository.buscarPorTermino(termino, pageable);
        } else {
            resultado = sedeRepository.findAll(pageable);
        }

        return ResponseEntity.ok(resultado);
    }

    // ✅ Crear sede
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sede> crear(@RequestBody Sede sede) {
        if (sede.getSban() == null || sede.getSban().isBlank()) {
            throw new IllegalArgumentException("El SBAN es obligatorio");
        }
        if (sedeRepository.findBySban(sede.getSban()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una sede con ese SBAN");
        }
        sede.setId(null);
        if (sede.getActiva() == null) sede.setActiva(true);
        return ResponseEntity.ok(sedeRepository.save(sede));
    }

    // ✅ Actualizar sede
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sede> actualizar(@PathVariable Long id, @RequestBody Sede datos) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        if (datos.getSban() != null && !datos.getSban().equals(sede.getSban())) {
            if (sedeRepository.findBySban(datos.getSban()).isPresent()) {
                throw new IllegalArgumentException("Ya existe otra sede con ese SBAN");
            }
            sede.setSban(datos.getSban());
        }

        if (datos.getNombreSede() != null) sede.setNombreSede(datos.getNombreSede());
        if (datos.getMunicipio() != null) sede.setMunicipio(datos.getMunicipio());
        if (datos.getRegional() != null) sede.setRegional(datos.getRegional());
        if (datos.getDepartamento() != null) sede.setDepartamento(datos.getDepartamento());
        if (datos.getDireccion() != null) sede.setDireccion(datos.getDireccion());
        if (datos.getHorario() != null) sede.setHorario(datos.getHorario());
        if (datos.getAtencion() != null) sede.setAtencion(datos.getAtencion());
        if (datos.getActiva() != null) sede.setActiva(datos.getActiva());

        return ResponseEntity.ok(sedeRepository.save(sede));
    }

    // ✅ Eliminar sede
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!sedeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sede no encontrada");
        }
        sedeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Carga masiva desde .csv o .txt
    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CargaMasivaSedeResponse> cargaMasiva(
            @RequestParam("archivo") MultipartFile archivo) throws IOException {

        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String nombre = archivo.getOriginalFilename();
        if (nombre == null) {
            throw new IllegalArgumentException("El archivo no tiene nombre");
        }

        String nombreLower = nombre.toLowerCase();
        if (!nombreLower.endsWith(".txt") && !nombreLower.endsWith(".csv")) {
            throw new IllegalArgumentException("Solo se aceptan archivos .txt o .csv");
        }

        return ResponseEntity.ok(sedeService.procesarCsv(archivo.getInputStream()));
    }
}