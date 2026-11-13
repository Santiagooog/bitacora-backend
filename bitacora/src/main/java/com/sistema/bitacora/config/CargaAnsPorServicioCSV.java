package com.sistema.bitacora.config;

import com.sistema.bitacora.entity.AnsPorServicio;
import com.sistema.bitacora.repository.AnsPorServicioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CargaAnsPorServicioCSV implements CommandLineRunner {

    private final AnsPorServicioRepository ansPorServicioRepository;

    @Override
    public void run(String... args) throws Exception {
        if (ansPorServicioRepository.count() > 0) {
            System.out.println("ANS por servicio ya cargados, omitiendo...");
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/ans_por_servicio.csv");
        if (!resource.exists()) {
            System.out.println("No se encontró ans_por_servicio.csv en resources/data/");
            return;
        }

        System.out.println("Cargando ANS por servicio desde CSV...");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            // Eliminar BOM si existe
            reader.mark(1);
            if (reader.read() != '\uFEFF') {
                reader.reset();
            }

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setQuote('"')
                    .setHeader("CodOficinas", "OficinasTipo", "Reg", "Oficina", "Departamento", "Ciudad", "Direccion", "Horario", "TiempoSdwan", "TiempoWifiPublico", "TiempoControlAcceso")
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();

            CSVParser parser = new CSVParser(reader, format);
            int contador = 0;

            for (CSVRecord record : parser) {
                try {
                    String codOficinas = record.get("CodOficinas");
                    if (codOficinas == null || codOficinas.isBlank()) continue;

                    AnsPorServicio ans = AnsPorServicio.builder()
                            .sban(codOficinas)
                            .oficinasTipo(record.get("OficinasTipo"))
                            .reg(record.get("Reg"))
                            .oficina(record.get("Oficina"))
                            .departamento(record.get("Departamento"))
                            .ciudad(record.get("Ciudad"))
                            .direccion(record.get("Direccion"))
                            .horario(record.get("Horario"))
                            .tiempoSdwan(parseInteger(record.get("TiempoSdwan")))
                            .tiempoWifiPublico(parseInteger(record.get("TiempoWifiPublico")))
                            .tiempoControlAcceso(parseInteger(record.get("TiempoControlAcceso")))
                            .build();

                    ansPorServicioRepository.save(ans);
                    contador++;
                } catch (Exception e) {
                    System.err.println("Error en registro ANS: " + e.getMessage());
                }
            }
            System.out.println("ANS por servicio cargados: " + contador);
        } catch (Exception e) {
            System.err.println("Error al cargar ANS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Valor no numérico: " + value);
            return null;
        }
    }
}