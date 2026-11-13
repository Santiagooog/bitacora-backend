package com.sistema.bitacora.config;

import com.sistema.bitacora.entity.AnsWifiPublico;
import com.sistema.bitacora.repository.AnsWifiPublicoRepository;
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
public class CargaAnsWifiPublicoCSV implements CommandLineRunner {

    private final AnsWifiPublicoRepository ansWifiPublicoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (ansWifiPublicoRepository.count() > 0) {
            System.out.println("ANS WiFi Público ya cargados, omitiendo...");
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/ans_wifi_publico.csv");
        if (!resource.exists()) {
            System.out.println("No se encontró ans_wifi_publico.csv en resources/data/");
            return;
        }

        System.out.println("Cargando ANS WiFi Público desde CSV...");

        // Usar UTF-8 para manejar BOM y caracteres especiales
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
                    .setHeader("SBAN", "CodDANE", "Oficina", "Municipio", "Departamento", "Tipo", "Direccion", "TiempoSolucion", "CantidadAPs")
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();

            CSVParser parser = new CSVParser(reader, format);
            int contador = 0;

            for (CSVRecord record : parser) {
                try {
                    String sban = record.get("SBAN");
                    if (sban == null || sban.isBlank()) continue;

                    AnsWifiPublico ans = AnsWifiPublico.builder()
                            .sban(sban)
                            .codDane(record.get("CodDANE"))
                            .oficina(record.get("Oficina"))
                            .municipio(record.get("Municipio"))
                            .departamento(record.get("Departamento"))
                            .tipo(record.get("Tipo"))
                            .direccion(record.get("Direccion"))
                            .tiempoSolucion(parseInteger(record.get("TiempoSolucion")))
                            .cantidadAps(parseInteger(record.get("CantidadAPs")))
                            .build();

                    ansWifiPublicoRepository.save(ans);
                    contador++;
                } catch (Exception e) {
                    System.err.println("Error en registro WiFi Público: " + e.getMessage());
                }
            }
            System.out.println("ANS WiFi Público cargados: " + contador);
        } catch (Exception e) {
            System.err.println("Error al cargar ANS WiFi Público: " + e.getMessage());
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