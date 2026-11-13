package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.puntoAcceso.CargaMasivaPuntoResponse;
import com.sistema.bitacora.entity.Activo;
import com.sistema.bitacora.entity.PuntoAcceso;
import com.sistema.bitacora.repository.ActivoRepository;
import com.sistema.bitacora.repository.PuntoAccesoRepository;
import com.sistema.bitacora.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PuntoAccesoService {

    private final PuntoAccesoRepository puntoAccesoRepository;
    private final SedeRepository sedeRepository;
    private final ActivoRepository activoRepository;

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    // ============================================================
    // CARGA MASIVA (crea en puntos_acceso y sincroniza el activo del proyecto correspondiente)
    // ============================================================
    @Transactional
    public CargaMasivaPuntoResponse procesarCsv(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        reader.mark(1);
        if (reader.read() != '\uFEFF') reader.reset();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setHeader("PLACA", "ESTADO", "MARCA", "TIPO", "MODELO", "DESCRIPCION",
                        "SERIE", "MAC", "SBAN", "OFICINA", "CIUDAD", "REGIONAL",
                        "AREA", "PISO", "FECHA DE INSTALACION", "FECHA INICIO SOPORTE",
                        "FECHA FIN DE SOPORTE", "NOMBRE FUNCIONARIO", "CEDULA", "CARGO",
                        "NOMBRE EN AXIOM XA", "FECHA MANTENIMIENTO 1", "FECHA MANTENIMIENTO 2",
                        "VESRION DE FIRMWARE", "PROYECTO")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .setAllowMissingColumnNames(true)
                .build();

        Set<String> seriesExistentes = new HashSet<>(puntoAccesoRepository.findAllSeries());
        Set<String> sbansValidos = new HashSet<>();
        sedeRepository.findAll().forEach(s -> sbansValidos.add(s.getSban()));

        int total = 0, creados = 0, omitidos = 0, errores = 0;
        List<String> detalles = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                total++;
                long linea = record.getRecordNumber() + 1;

                try {
                    String tipo = get(record, "TIPO").toUpperCase();

                    if (!"CONTROLADORA".equals(tipo) && !"LECTORA".equals(tipo)) {
                        omitidos++;
                        continue;
                    }

                    String serie = get(record, "SERIE");
                    if (serie.isBlank() || "N/A".equalsIgnoreCase(serie)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SERIE vacía o N/A");
                        continue;
                    }

                    if (seriesExistentes.contains(serie)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": serie duplicada (" + serie + ")");
                        continue;
                    }

                    String sban = get(record, "SBAN");
                    if (!sban.isBlank() && !sbansValidos.contains(sban)) {
                        omitidos++;
                        detalles.add("Línea " + linea + ": SBAN no existe en sedes (" + sban + ")");
                        continue;
                    }

                    String proyecto = normalizarProyecto(get(record, "PROYECTO"));
                    if (proyecto == null) {
                        throw new IllegalArgumentException("PROYECTO debe ser S1528 o S1528A");
                    }

                    // ✅ 1. Guardar en puntos_acceso
                    PuntoAcceso p = PuntoAcceso.builder()
                            .placa(get(record, "PLACA"))
                            .estado(get(record, "ESTADO"))
                            .marca(get(record, "MARCA"))
                            .tipo(tipo)
                            .modelo(get(record, "MODELO"))
                            .descripcion(get(record, "DESCRIPCION"))
                            .serie(serie)
                            .mac(get(record, "MAC"))
                            .sban(sban)
                            .oficina(get(record, "OFICINA"))
                            .ciudad(get(record, "CIUDAD"))
                            .regional(get(record, "REGIONAL"))
                            .area(get(record, "AREA"))
                            .piso(parseInt(get(record, "PISO")))
                            .fechaInstalacion(parseDate(get(record, "FECHA DE INSTALACION")))
                            .fechaInicioSoporte(parseDate(get(record, "FECHA INICIO SOPORTE")))
                            .fechaFinSoporte(parseDate(get(record, "FECHA FIN DE SOPORTE")))
                            .nombreFuncionario(get(record, "NOMBRE FUNCIONARIO"))
                            .cedula(get(record, "CEDULA"))
                            .cargo(get(record, "CARGO"))
                            .nombreAxiom(get(record, "NOMBRE EN AXIOM XA"))
                            .fechaMantenimiento1(get(record, "FECHA MANTENIMIENTO 1"))
                            .fechaMantenimiento2(get(record, "FECHA MANTENIMIENTO 2"))
                            .versionFirmware(get(record, "VESRION DE FIRMWARE"))
                            .proyecto(proyecto)
                            .activo(true)
                            .build();
                    puntoAccesoRepository.save(p);
                    seriesExistentes.add(serie);

                    // ✅ 2. Guardar también en activos con el tipo de servicio del proyecto CAS.
                    Activo activoExistente = activoRepository.findBySerial(serie).orElse(null);
                    if (activoExistente == null || esActivoCas(activoExistente)) {
                        sincronizarActivoDesdePunto(p, activoExistente);
                    }

                    creados++;
                } catch (Exception e) {
                    errores++;
                    detalles.add("Línea " + linea + ": " + e.getMessage());
                }
            }
        }

        return new CargaMasivaPuntoResponse(total, creados, omitidos, errores, detalles);
    }

    // ============================================================
    // SINCRONIZACIÓN MANUAL (normaliza los proyectos y actualiza sus activos CAS)
    // ============================================================
    @Transactional
    public int sincronizarAActivos() {
        List<PuntoAcceso> todos = puntoAccesoRepository.findAll();

        int sincronizados = 0;
        for (PuntoAcceso p : todos) {
            if (p.getSerie() == null || p.getSerie().isBlank()) continue;

            String proyecto = normalizarProyecto(p.getProyecto());
            if (proyecto == null) continue;
            if (!proyecto.equals(p.getProyecto())) {
                p.setProyecto(proyecto);
                puntoAccesoRepository.save(p);
            }

            Activo activo = activoRepository.findBySerial(p.getSerie()).orElse(null);
            if (activo != null && !esActivoCas(activo)) continue;
            if (tipoServicioPorProyecto(p.getProyecto()) == null) continue;
            sincronizarActivoDesdePunto(p, activo);
            sincronizados++;
        }
        return sincronizados;
    }

    private void sincronizarActivoDesdePunto(PuntoAcceso punto, Activo activo) {
        if (activo == null) {
            activo = Activo.builder().serial(punto.getSerie()).build();
        }
        activo.setName(punto.getArea() != null && !punto.getArea().isBlank()
                ? punto.getArea() : punto.getOficina());
        activo.setSban(punto.getSban());
        activo.setSite(punto.getNombreAxiom());
        activo.setDeviceType(punto.getModelo());
        activo.setPlaca(punto.getPlaca());
        String proyecto = normalizarProyecto(punto.getProyecto());
        activo.setProyecto(proyecto);
        activo.setTipoServicio(tipoServicioPorProyecto(proyecto));
        activo.setActivo(true);
        activoRepository.save(activo);
    }

    private boolean esActivoCas(Activo activo) {
        String tipoServicio = activo.getTipoServicio();
        return tipoServicio != null && (tipoServicio.equalsIgnoreCase("ACCESOS")
                || tipoServicio.equalsIgnoreCase("CAS_1528")
                || tipoServicio.equalsIgnoreCase("CAS_1528A"));
    }

    private String normalizarProyecto(String proyecto) {
        if (proyecto == null || proyecto.isBlank()) return null;
        String clave = proyecto.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (clave.contains("1528A")) return "CAS 1528A";
        if (clave.contains("1528")) return "CAS 1528";
        return null;
    }

    private String tipoServicioPorProyecto(String proyecto) {
        String normalizado = normalizarProyecto(proyecto);
        if ("CAS 1528A".equals(normalizado)) return "CAS_1528A";
        if ("CAS 1528".equals(normalizado)) return "CAS_1528";
        return null;
    }

    // ============================================================
    // Helpers
    // ============================================================
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

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(s.trim(), fmt);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
