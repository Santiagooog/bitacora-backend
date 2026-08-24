package com.sistema.bitacora.config;

import com.sistema.bitacora.entity.Sede;
import com.sistema.bitacora.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CargaSedesCSV implements CommandLineRunner {

    private final SedeRepository sedeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (sedeRepository.count() > 0) {
            return; // ya hay sedes cargadas
        }

        ClassPathResource resource = new ClassPathResource("data/sedes.csv");
        if (!resource.exists()) {
            System.out.println("No se encontró sedes.csv en resources/data/");
            return;
        }

        // Usar ISO-8859-1 para caracteres latinos
        Charset charset = StandardCharsets.ISO_8859_1;

        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), charset)) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setQuote('"')
                    .setHeader("SBAN", "SEDE", "MUNICIPIO", "REGIONAL", "DPTO", "Direccion", "HORARIO", "ATENCION")
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setAllowMissingColumnNames(true)   // clave para ignorar columnas vacías
                    .build();

            CSVParser parser = new CSVParser(reader, format);
            int contador = 0;

            for (CSVRecord record : parser) {
                try {
                    String sban = record.get("SBAN");
                    String nombreSede = record.get("SEDE");
                    if (sban == null || sban.isBlank() || nombreSede == null || nombreSede.isBlank()) {
                        continue;
                    }

                    Sede sede = Sede.builder()
                            .sban(sban)
                            .nombreSede(nombreSede)
                            .municipio(record.get("MUNICIPIO"))
                            .regional(record.get("REGIONAL"))
                            .departamento(record.get("DPTO"))
                            .direccion(record.get("Direccion"))
                            .horario(record.get("HORARIO"))
                            .atencion(record.get("ATENCION"))
                            .activa(true)
                            .build();

                    sedeRepository.save(sede);
                    contador++;
                } catch (Exception e) {
                    System.err.println("Error en registro: " + e.getMessage());
                }
            }
            System.out.println("Sedes cargadas: " + contador);
        } catch (Exception e) {
            System.err.println("Error al cargar sedes desde CSV: " + e.getMessage());
        }
    }
}