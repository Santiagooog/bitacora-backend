package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.activo.CargaMasivaResponse;
import com.sistema.bitacora.entity.Activo;
import com.sistema.bitacora.repository.ActivoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;   // ✅ NUEVO
import java.util.List;
import java.util.Set;       // ✅ NUEVO

@Service
@RequiredArgsConstructor
public class ActivoService {

    private final ActivoRepository activoRepository;

    public CargaMasivaResponse procesarCsv(InputStream is, String tipoServicio) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));

        // Eliminar BOM UTF-8
        reader.mark(1);
        if (reader.read() != '\uFEFF') {
            reader.reset();
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader("ESN", "Name", "Sban", "Site", "DeviceType", "Plate")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        // ✅ NUEVO — cargar todos los seriales existentes UNA SOLA VEZ
        Set<String> serialesExistentes = new HashSet<>(activoRepository.findAllSeriales());

        int total = 0, creados = 0, omitidos = 0, errores = 0;
        List<String> detalles = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                total++;
                long linea = record.getRecordNumber() + 1;

                try {
                    String esn = record.get("ESN").trim();

                    if (esn.isBlank()) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": ESN vacío");
                        continue;
                    }

                    // ✅ NUEVO — validación en memoria, ya no consulta la BD
                    if (serialesExistentes.contains(esn)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": serial duplicado (" + esn + ")");
                        continue;
                    }

                    String placa = "";
                    try {
                        placa = record.get("Plate").trim();
                    } catch (RuntimeException ignored) {
                        // Plate es opcional para conservar compatibilidad con archivos anteriores.
                    }

                    Activo activo = Activo.builder()
                            .serial(esn)
                            .placa(placa.isBlank() ? null : placa)
                            .name(record.get("Name").trim())
                            .sban(record.get("Sban").trim())
                            .site(record.get("Site").trim())
                            .deviceType(record.get("DeviceType").trim())
                            .proyecto("CAS_1528A".equalsIgnoreCase(tipoServicio) ? "CAS 1528A"
                                    : "CAS_1528".equalsIgnoreCase(tipoServicio) ? "CAS 1528" : null)
                            .tipoServicio(tipoServicio)
                            .activo(true)
                            .build();

                    activoRepository.save(activo);
                    serialesExistentes.add(esn);   // ✅ NUEVO — evita duplicados dentro del mismo archivo
                    creados++;
                } catch (Exception e) {
                    errores++;
                    detalles.add("Línea " + linea + ": " + e.getMessage());
                }
            }
        }

        return new CargaMasivaResponse(total, creados, omitidos, errores, detalles);
    }
}
