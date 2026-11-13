package com.sistema.bitacora.controller;


import com.sistema.bitacora.dto.ans.CargaMasivaAnsResponse;
import com.sistema.bitacora.entity.AnsPorServicio;
import com.sistema.bitacora.repository.AnsPorServicioRepository;
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
import com.sistema.bitacora.service.AnsService;

import java.util.List;

@RestController
@RequestMapping("/api/ans")
@RequiredArgsConstructor
public class AnsController {

    private final AnsPorServicioRepository ansRepository;
    private final AnsService ansService;

    // Listar con paginación y búsqueda
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AnsPorServicio>> listar(
            @RequestParam(defaultValue = "") String termino,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("oficina").ascending());
        Page<AnsPorServicio> ansPage;

        if (termino.isBlank()) {
            ansPage = ansRepository.findAll(pageable);
        } else {
            ansPage = ansRepository.buscarPorTermino(termino, pageable);
        }

        return ResponseEntity.ok(ansPage);
    }

    // Obtener uno por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnsPorServicio> obtener(@PathVariable Long id) {
        return ansRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Editar un ANS
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnsPorServicio> actualizar(
            @PathVariable Long id,
            @RequestBody AnsPorServicio datos) {

        AnsPorServicio ans = ansRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ANS no encontrado"));

        ans.setTiempoSdwan(datos.getTiempoSdwan());
        ans.setTiempoWifiPublico(datos.getTiempoWifiPublico());
        ans.setTiempoControlAcceso(datos.getTiempoControlAcceso());
        // Actualiza otros campos si es necesario

        return ResponseEntity.ok(ansRepository.save(ans));
    }

    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CargaMasivaAnsResponse> cargaMasiva(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") String tipo) throws Exception {

        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null) {
            throw new IllegalArgumentException("El archivo no tiene nombre");
        }
        String lower = nombre.toLowerCase();
        if (!lower.endsWith(".txt") && !lower.endsWith(".csv")) {
            throw new IllegalArgumentException("Solo se aceptan archivos .txt o .csv");
        }

        List<String> tiposValidos = List.of("POR_SERVICIO", "SDWAN", "WIFI_PUBLICO", "CONTROL_ACCESO");
        if (!tiposValidos.contains(tipo.toUpperCase())) {
            throw new IllegalArgumentException("Tipo ANS inválido: " + tipo);
        }

        return ResponseEntity.ok(ansService.procesarCsv(archivo.getInputStream(), tipo));
    }
}