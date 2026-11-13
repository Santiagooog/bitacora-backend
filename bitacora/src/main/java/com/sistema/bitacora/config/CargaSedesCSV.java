package com.sistema.bitacora.config;

import com.sistema.bitacora.dto.sede.CargaMasivaSedeResponse;
import com.sistema.bitacora.repository.SedeRepository;
import com.sistema.bitacora.service.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CargaSedesCSV implements CommandLineRunner {

    private final SedeRepository sedeRepository;
    private final SedeService sedeService;

    @Override
    public void run(String... args) throws Exception {
        if (sedeRepository.count() > 0) {
            System.out.println("Sedes ya cargadas, omitiendo...");
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/sedes.csv");
        if (!resource.exists()) {
            System.out.println("No se encontró sedes.csv en resources/data/");
            return;
        }

        try {
            CargaMasivaSedeResponse r = sedeService.procesarCsv(resource.getInputStream());
            System.out.println("Sedes cargadas: " + r.creados()
                    + " (omitidos: " + r.omitidos() + ", errores: " + r.errores() + ")");
        } catch (Exception e) {
            System.err.println("Error al cargar sedes desde CSV: " + e.getMessage());
        }
    }
}