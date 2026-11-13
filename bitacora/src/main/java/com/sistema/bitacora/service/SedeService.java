package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.sede.CargaMasivaSedeResponse;
import com.sistema.bitacora.entity.Sede;
import com.sistema.bitacora.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;

    public CargaMasivaSedeResponse procesarCsv(InputStream is) throws IOException {
        // Encoding latin-1 (el CSV viene así desde la fuente original)
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setHeader("SBAN", "SEDE", "MUNICIPIO", "REGIONAL", "DPTO",
                        "Direccion", "HORARIO", "ATENCION")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .setAllowMissingColumnNames(true)   // ignora las ;;;; del final
                .build();

        // Cargar todos los SBAN existentes UNA SOLA VEZ
        Set<String> sbansExistentes = new HashSet<>(sedeRepository.findAllSbans());

        int total = 0, creados = 0, omitidos = 0, errores = 0;
        List<String> detalles = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                total++;
                long linea = record.getRecordNumber() + 1;

                try {
                    String sban = record.get("SBAN") == null ? "" : record.get("SBAN").trim();
                    String nombreSede = record.get("SEDE") == null ? "" : record.get("SEDE").trim();

                    if (sban.isBlank()) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN vacío");
                        continue;
                    }
                    if (nombreSede.isBlank()) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SEDE vacío");
                        continue;
                    }
                    if (sbansExistentes.contains(sban)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN duplicado (" + sban + ")");
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
                    sbansExistentes.add(sban);
                    creados++;
                } catch (Exception e) {
                    errores++;
                    detalles.add("Línea " + linea + ": " + e.getMessage());
                }
            }
        }

        return new CargaMasivaSedeResponse(total, creados, omitidos, errores, detalles);
    }
}