package com.sistema.bitacora.config;

import com.sistema.bitacora.dto.activo.CargaMasivaResponse;
import com.sistema.bitacora.service.ActivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CargaActivosCSV implements CommandLineRunner {

    private final ActivoService activoService;

    @Override
    public void run(String... args) throws Exception {
        cargar("data/activos_sdwan.csv", "SDWAN");
        cargar("data/activos_wifi.csv", "WIFI");
    }

    private void cargar(String ruta, String tipoServicio) {
        ClassPathResource resource = new ClassPathResource(ruta);
        if (!resource.exists()) {
            System.out.println("No se encontró " + ruta);
            return;
        }

        try {
            CargaMasivaResponse r = activoService.procesarCsv(
                    resource.getInputStream(), tipoServicio);
            System.out.println("Activos " + tipoServicio + " cargados: " + r.creados()
                    + " (omitidos: " + r.omitidos() + ", errores: " + r.errores() + ")");
        } catch (Exception e) {
            System.err.println("Error al cargar " + tipoServicio + ": " + e.getMessage());
        }
    }
}