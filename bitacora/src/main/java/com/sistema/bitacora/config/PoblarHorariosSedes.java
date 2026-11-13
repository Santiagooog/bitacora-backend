package com.sistema.bitacora.config;

import com.sistema.bitacora.entity.Sede;
import com.sistema.bitacora.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PoblarHorariosSedes implements CommandLineRunner {

    private final SedeRepository sedeRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo ejecutar si hay sedes sin horario estructurado
        List<Sede> sedes = sedeRepository.findAll();
        for (Sede sede : sedes) {
            if (sede.getHoraInicioManana() == null) {
                // Intentar parsear el horario de texto
                parsearYAsignarHorario(sede);
                sedeRepository.save(sede);
            }
        }
    }

    private void parsearYAsignarHorario(Sede sede) {
        String horario = sede.getHorario();
        if (horario == null || horario.isBlank()) {
            // Si no hay horario, asignamos uno por defecto
            asignarHorarioPorDefecto(sede);
            return;
        }

        // Ejemplo: "LUNES A VIERNES 8:00 A.M. A 12:00 P.M. Y 2:00 P.M. A 4:30 P.M."
        // Intentar encontrar horas
        Pattern pattern = Pattern.compile("(\\d{1,2}:\\d{2})\\s*(A\\.M\\.|P\\.M\\.)?\\s*(A|Y)?\\s*(\\d{1,2}:\\d{2})?\\s*(A\\.M\\.|P\\.M\\.)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(horario);

        LocalTime inicioManana = null;
        LocalTime finManana = null;
        LocalTime inicioTarde = null;
        LocalTime finTarde = null;

        int contador = 0;
        while (matcher.find() && contador < 4) {
            String horaStr = matcher.group(1);
            String amPm = matcher.group(2);
            LocalTime hora = parseHora(horaStr, amPm);

            if (contador == 0) inicioManana = hora;
            else if (contador == 1) finManana = hora;
            else if (contador == 2) inicioTarde = hora;
            else if (contador == 3) finTarde = hora;
            contador++;
        }

        // Si no se pudo parsear, asignar horario común de 8-12 y 2-4:30
        if (inicioManana == null) {
            inicioManana = LocalTime.of(8, 0);
            finManana = LocalTime.of(12, 0);
            inicioTarde = LocalTime.of(14, 0);
            finTarde = LocalTime.of(16, 30);
        } else if (finManana == null) {
            finManana = inicioManana.plusHours(4);
        } else if (inicioTarde == null) {
            inicioTarde = finManana.plusHours(2);
            finTarde = inicioTarde.plusHours(2).plusMinutes(30);
        }

        sede.setHoraInicioManana(inicioManana);
        sede.setHoraFinManana(finManana);
        sede.setHoraInicioTarde(inicioTarde);
        sede.setHoraFinTarde(finTarde);
    }

    private LocalTime parseHora(String horaStr, String amPm) {
        String[] partes = horaStr.split(":");
        int hora = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);

        if (amPm != null && amPm.equalsIgnoreCase("P.M.") && hora < 12) {
            hora += 12;
        } else if (amPm != null && amPm.equalsIgnoreCase("A.M.") && hora == 12) {
            hora = 0;
        }
        return LocalTime.of(hora, minutos);
    }

    private void asignarHorarioPorDefecto(Sede sede) {
        sede.setHoraInicioManana(LocalTime.of(8, 0));
        sede.setHoraFinManana(LocalTime.of(12, 0));
        sede.setHoraInicioTarde(LocalTime.of(14, 0));
        sede.setHoraFinTarde(LocalTime.of(16, 30));
    }
}