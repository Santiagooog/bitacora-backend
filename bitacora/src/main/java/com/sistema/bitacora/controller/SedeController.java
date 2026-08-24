package com.sistema.bitacora.controller;

import com.sistema.bitacora.entity.Sede;
import com.sistema.bitacora.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sedes")
@CrossOrigin(origins = "*")
public class SedeController {

    @Autowired
    private SedeRepository sedeRepository;

    @GetMapping("/sban/{sban}")
    public ResponseEntity<Sede> obtenerSedePorSban(@PathVariable String sban) {
        return sedeRepository.findBySban(sban)
                .map(sede -> ResponseEntity.ok().body(sede))
                .orElse(ResponseEntity.notFound().build());
    }
}