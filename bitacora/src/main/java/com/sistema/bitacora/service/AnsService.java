package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.ans.CargaMasivaAnsResponse;
import com.sistema.bitacora.entity.AnsPorServicio;
import com.sistema.bitacora.entity.AnsSdwan;
import com.sistema.bitacora.entity.AnsWifiPublico;
import com.sistema.bitacora.repository.AnsPorServicioRepository;
import com.sistema.bitacora.repository.AnsSdwanRepository;
import com.sistema.bitacora.repository.AnsWifiPublicoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnsService {

    private final AnsPorServicioRepository porServicioRepository;
    private final AnsSdwanRepository sdwanRepository;
    private final AnsWifiPublicoRepository wifiPublicoRepository;

    @Transactional
    public CargaMasivaAnsResponse procesarCsv(InputStream is, String tipo) throws Exception {
        return switch (tipo.toUpperCase()) {
            case "POR_SERVICIO" -> procesarPorServicio(is);
            case "SDWAN" -> procesarSdwan(is);
            case "WIFI_PUBLICO" -> procesarWifiPublico(is);
            case "CONTROL_ACCESO" -> procesarControlAcceso(is);   // ← AGREGAR ESTA LÍNEA
            default -> throw new IllegalArgumentException("Tipo ANS inválido: " + tipo);
        };
    }

    // ============================================================
    // ANS POR SERVICIO
    // ============================================================
    private CargaMasivaAnsResponse procesarPorServicio(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        reader.mark(1);
        if (reader.read() != '\uFEFF') reader.reset();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setHeader("COD OFICINAS", "OFICINAS", "REG", "OFICINA", "Departamento",
                        "Ciudad", "Dirección", "HORARIO",
                        "TIEMPO SOLUCION SDWAN", "TIEMPO SOLUCION WIFI PUBLICO",
                        "TIEMPO SOLUCION CONTROL DE ACCESO")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .setAllowMissingColumnNames(true)
                .build();

        Set<String> sbansExistentes = new HashSet<>();
        porServicioRepository.findAll().forEach(a -> {
            if (a.getSban() != null) sbansExistentes.add(a.getSban());
        });

        int total = 0, creados = 0, omitidos = 0, errores = 0;
        List<String> detalles = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                total++;
                long linea = record.getRecordNumber() + 1;

                try {
                    String sban = get(record, "COD OFICINAS");
                    if (sban.isBlank()) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN vacío");
                        continue;
                    }
                    if (sbansExistentes.contains(sban)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN duplicado (" + sban + ")");
                        continue;
                    }

                    AnsPorServicio ans = AnsPorServicio.builder()
                            .sban(sban)
                            .oficinasTipo(get(record, "OFICINAS"))
                            .reg(get(record, "REG"))
                            .oficina(get(record, "OFICINA"))
                            .departamento(get(record, "Departamento"))
                            .ciudad(get(record, "Ciudad"))
                            .direccion(get(record, "Dirección"))
                            .horario(get(record, "HORARIO"))
                            .tiempoSdwan(parseInt(get(record, "TIEMPO SOLUCION SDWAN")))
                            .tiempoWifiPublico(parseInt(get(record, "TIEMPO SOLUCION WIFI PUBLICO")))
                            .tiempoControlAcceso(parseInt(get(record, "TIEMPO SOLUCION CONTROL DE ACCESO")))
                            .build();

                    porServicioRepository.save(ans);
                    sbansExistentes.add(sban);
                    creados++;
                } catch (Exception e) {
                    errores++;
                    detalles.add("Línea " + linea + ": " + e.getMessage());
                }
            }
        }

        return new CargaMasivaAnsResponse(total, creados, omitidos, errores, detalles);
    }

    // ============================================================
    // ANS SDWAN
    // ============================================================
    private CargaMasivaAnsResponse procesarSdwan(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        reader.mark(1);
        if (reader.read() != '\uFEFF') reader.reset();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setHeader("COD OFICINAS", "OFICINAS", "REG", "OFICINA", "Departamento",
                        "Ciudad", "Dirección", "HORARIO", "TIEMPO SOLUCION")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .setAllowMissingColumnNames(true)
                .build();

        Set<String> sbansExistentes = new HashSet<>();
        sdwanRepository.findAll().forEach(a -> {
            if (a.getSban() != null) sbansExistentes.add(a.getSban());
        });

        int total = 0, creados = 0, omitidos = 0, errores = 0;
        List<String> detalles = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                total++;
                long linea = record.getRecordNumber() + 1;

                try {
                    String sban = get(record, "COD OFICINAS");
                    if (sban.isBlank()) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN vacío");
                        continue;
                    }
                    if (sbansExistentes.contains(sban)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN duplicado (" + sban + ")");
                        continue;
                    }

                    AnsSdwan ans = AnsSdwan.builder()
                            .sban(sban)
                            .oficinasTipo(get(record, "OFICINAS"))
                            .reg(get(record, "REG"))
                            .oficina(get(record, "OFICINA"))
                            .departamento(get(record, "Departamento"))
                            .ciudad(get(record, "Ciudad"))
                            .direccion(get(record, "Dirección"))
                            .horario(get(record, "HORARIO"))
                            .tiempoSolucion(parseInt(get(record, "TIEMPO SOLUCION")))
                            .build();

                    sdwanRepository.save(ans);
                    sbansExistentes.add(sban);
                    creados++;
                } catch (Exception e) {
                    errores++;
                    detalles.add("Línea " + linea + ": " + e.getMessage());
                }
            }
        }

        return new CargaMasivaAnsResponse(total, creados, omitidos, errores, detalles);
    }


    // ============================================================
    // ANS WIFI PUBLICO (leer por índice por header raro)
    // ============================================================
    @Transactional
    private CargaMasivaAnsResponse procesarWifiPublico(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        reader.mark(1);
        if (reader.read() != '\uFEFF') reader.reset();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        Set<String> sbansExistentes = new HashSet<>();
        wifiPublicoRepository.findAll().forEach(a -> {
            if (a.getSban() != null) sbansExistentes.add(a.getSban());
        });

        int total = 0, creados = 0, omitidos = 0, errores = 0;
        List<String> detalles = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                total++;
                long linea = record.getRecordNumber() + 1;

                try {
                    if (record.size() < 9) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": columnas insuficientes (" + record.size() + ")");
                        continue;
                    }

                    String sban = record.get(0).trim();
                    if (sban.isBlank()) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN vacío");
                        continue;
                    }
                    if (sbansExistentes.contains(sban)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN duplicado (" + sban + ")");
                        continue;
                    }

                    AnsWifiPublico ans = AnsWifiPublico.builder()
                            .sban(sban)
                            .codDane(record.get(1).trim())
                            .oficina(record.get(2).trim())
                            .municipio(record.get(3).trim())
                            .departamento(record.get(4).trim())
                            .tipo(record.get(5).trim())
                            .direccion(record.get(6).trim())
                            .tiempoSolucion(parseInt(record.get(7).trim()))
                            .cantidadAps(parseInt(record.get(8).trim()))
                            .build();

                    wifiPublicoRepository.save(ans);
                    sbansExistentes.add(sban);
                    creados++;
                } catch (Exception e) {
                    errores++;
                    detalles.add("Línea " + linea + ": " + e.getMessage());
                }
            }
        }

        return new CargaMasivaAnsResponse(total, creados, omitidos, errores, detalles);
    }

    // ============================================================
// ANS CONTROL DE ACCESO
// Actualiza el campo tiempoControlAcceso en AnsPorServicio.
// Formato CSV:
// COD OFICINAS;OFICINAS;REG;OFICINA;Departamento;Ciudad;Dirección;HORARIO;TIEMPO SOLUCION CONTROL DE ACCESO
// ============================================================
    private CargaMasivaAnsResponse procesarControlAcceso(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        reader.mark(1);
        if (reader.read() != '\uFEFF') reader.reset();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setHeader("SBAN", "TIEMPO SOLUCION CONTROL DE ACCESO")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        int total = 0, creados = 0, omitidos = 0, errores = 0;
        List<String> detalles = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                total++;
                long linea = record.getRecordNumber() + 1;
                try {
                    String sban = get(record, "SBAN");
                    if (sban.isBlank()) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN vacío");
                        continue;
                    }
                    AnsPorServicio ans = porServicioRepository.findBySban(sban).orElse(null);
                    if (ans == null) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN no existe (" + sban + ")");
                        continue;
                    }
                    ans.setTiempoControlAcceso(parseInt(get(record, "TIEMPO SOLUCION CONTROL DE ACCESO")));
                    porServicioRepository.save(ans);
                    creados++;
                } catch (Exception e) {
                    errores++;
                    detalles.add("Línea " + linea + ": " + e.getMessage());
                }
            }
        }
        return new CargaMasivaAnsResponse(total, creados, omitidos, errores, detalles);
    }

    // Helpers
    private String get(CSVRecord record, String column) {
        try {
            String v = record.get(column);
            return v == null ? "" : v.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            if (s.contains(".")) s = s.substring(0, s.indexOf("."));
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}