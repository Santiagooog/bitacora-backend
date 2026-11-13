package com.sistema.bitacora.service;

import com.sistema.bitacora.entity.AnsPorServicio;
import com.sistema.bitacora.entity.Caso;
import com.sistema.bitacora.entity.Sede;
import com.sistema.bitacora.entity.Servicio;
import com.sistema.bitacora.repository.AnsPorServicioRepository;
import com.sistema.bitacora.repository.SedeRepository;
import com.sistema.bitacora.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AnssService {

    private static final int HORAS_SERVICIO_POR_DIA = 8;
    private static final Pattern ACENTOS = Pattern.compile("\\p{M}+");

    private final SedeRepository sedeRepository;
    private final ServicioRepository servicioRepository;
    private final AnsPorServicioRepository ansPorServicioRepository;

    public LocalDateTime calcularVencimiento(LocalDateTime fechaInicio, Long servicioId, Long sedeId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        Sede sede = sedeRepository.findById(sedeId)
                .orElseThrow(() -> new RuntimeException("Sede no encontrada"));

        String nombreServicio = servicio.getNombre().toUpperCase(Locale.ROOT);
        AnsPorServicio ans = ansPorServicioRepository.findBySban(sede.getSban()).orElse(null);
        Integer horasAns = obtenerHorasAns(nombreServicio, ans);
        if (horasAns == null) horasAns = 24;

        if (esSdwan(nombreServicio)) {
            return fechaInicio.plusHours(horasAns);
        }

        if (esWifiOCas(nombreServicio)) {
            String horario = ans != null && ans.getHorario() != null && !ans.getHorario().isBlank()
                    ? ans.getHorario() : sede.getHorario();
            return calcularVencimientoPorDiasAbiertos(fechaInicio, horasAns, obtenerDiasAbiertos(horario));
        }

        // Los demás servicios conservan el cálculo hábil anterior basado en las horas de la sede.
        return calcularVencimientoHabilExistente(fechaInicio, sede, horasAns);
    }

    /** Devuelve el saldo de ANS activo, en segundos, para la consulta de detalle del caso. */
    public long calcularSegundosRestantes(Caso caso, LocalDateTime ahora) {
        if (caso.getFechaVencimiento() == null || caso.getServicio() == null || caso.getSedePrincipal() == null) {
            return 0;
        }
        if (!caso.getFechaVencimiento().isAfter(ahora)) return 0;

        String nombreServicio = caso.getServicio().getNombre().toUpperCase(Locale.ROOT);
        if (esSdwan(nombreServicio)) {
            return Duration.between(ahora, caso.getFechaVencimiento()).getSeconds();
        }
        if (!esWifiOCas(nombreServicio)) {
            return Duration.between(ahora, caso.getFechaVencimiento()).getSeconds();
        }

        AnsPorServicio ans = ansPorServicioRepository.findBySban(caso.getSedePrincipal().getSban()).orElse(null);
        String horario = ans != null && ans.getHorario() != null && !ans.getHorario().isBlank()
                ? ans.getHorario() : caso.getSedePrincipal().getHorario();
        Set<DayOfWeek> diasAbiertos = obtenerDiasAbiertos(horario);
        LocalTime horaInicioDiaria = caso.getFechaSolicitud().toLocalTime();

        long segundos = 0;
        for (LocalDate dia = ahora.toLocalDate(); !dia.isAfter(caso.getFechaVencimiento().toLocalDate()); dia = dia.plusDays(1)) {
            if (!diasAbiertos.contains(dia.getDayOfWeek())) continue;
            LocalDateTime inicioJornada = dia.atTime(horaInicioDiaria);
            LocalDateTime finJornada = inicioJornada.plusHours(HORAS_SERVICIO_POR_DIA);
            LocalDateTime desde = inicioJornada.isAfter(ahora) ? inicioJornada : ahora;
            LocalDateTime hasta = finJornada.isBefore(caso.getFechaVencimiento())
                    ? finJornada : caso.getFechaVencimiento();
            if (hasta.isAfter(desde)) segundos += Duration.between(desde, hasta).getSeconds();
        }
        return segundos;
    }

    private Integer obtenerHorasAns(String nombreServicio, AnsPorServicio ans) {
        if (ans == null) return null;
        if (nombreServicio.contains("WIFI")) return ans.getTiempoWifiPublico();
        if (esSdwan(nombreServicio)) return ans.getTiempoSdwan();
        if (esWifiOCas(nombreServicio)) return ans.getTiempoControlAcceso();
        return null;
    }

    private boolean esSdwan(String nombreServicio) {
        return nombreServicio.contains("SDWAN");
    }

    private boolean esWifiOCas(String nombreServicio) {
        return nombreServicio.contains("WIFI") || nombreServicio.contains("CAS 1528")
                || nombreServicio.contains("CONTROL DE ACCESO");
    }

    private LocalDateTime calcularVencimientoPorDiasAbiertos(LocalDateTime inicio, int horasAns,
                                                               Set<DayOfWeek> diasAbiertos) {
        LocalDateTime cursor = inicio;
        long segundosRestantes = Duration.ofHours(horasAns).getSeconds();
        LocalTime horaInicioDiaria = inicio.toLocalTime();
        int diasRevisados = 0;

        while (segundosRestantes > 0) {
            if (++diasRevisados > 3660) throw new IllegalStateException("No se pudo calcular el vencimiento del ANS");
            LocalDate dia = cursor.toLocalDate();
            if (!diasAbiertos.contains(dia.getDayOfWeek())) {
                cursor = dia.plusDays(1).atTime(horaInicioDiaria);
                continue;
            }

            LocalDateTime inicioJornada = dia.atTime(horaInicioDiaria);
            LocalDateTime finJornada = inicioJornada.plusHours(HORAS_SERVICIO_POR_DIA);
            if (cursor.isBefore(inicioJornada)) cursor = inicioJornada;
            if (!cursor.isBefore(finJornada)) {
                cursor = dia.plusDays(1).atTime(horaInicioDiaria);
                continue;
            }

            long disponibles = Duration.between(cursor, finJornada).getSeconds();
            if (segundosRestantes <= disponibles) return cursor.plusSeconds(segundosRestantes);
            segundosRestantes -= disponibles;
            cursor = dia.plusDays(1).atTime(horaInicioDiaria);
        }
        return cursor;
    }

    private Set<DayOfWeek> obtenerDiasAbiertos(String horario) {
        String normalizado = normalizar(horario);
        if (normalizado.contains("7X24") || normalizado.contains("24X7")) {
            return EnumSet.allOf(DayOfWeek.class);
        }

        String[] partes = normalizado.split("[^A-Z]+");
        DayOfWeek primero = null;
        DayOfWeek ultimo = null;
        for (String parte : partes) {
            DayOfWeek dia = convertirDia(parte);
            if (dia != null) {
                if (primero == null) primero = dia;
                ultimo = dia;
            }
        }
        if (primero == null || ultimo == null) {
            return EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        }

        EnumSet<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        DayOfWeek actual = primero;
        while (true) {
            dias.add(actual);
            if (actual == ultimo) break;
            actual = actual.plus(1);
        }
        return dias;
    }

    private DayOfWeek convertirDia(String texto) {
        if (texto.startsWith("LUN")) return DayOfWeek.MONDAY;
        if (texto.startsWith("MAR")) return DayOfWeek.TUESDAY;
        if (texto.startsWith("MIE")) return DayOfWeek.WEDNESDAY;
        if (texto.startsWith("JUE")) return DayOfWeek.THURSDAY;
        if (texto.startsWith("VIE")) return DayOfWeek.FRIDAY;
        if (texto.startsWith("SAB")) return DayOfWeek.SATURDAY;
        if (texto.startsWith("DOM")) return DayOfWeek.SUNDAY;
        return null;
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        String descompuesto = Normalizer.normalize(valor.toUpperCase(Locale.ROOT), Normalizer.Form.NFD);
        return ACENTOS.matcher(descompuesto).replaceAll("").replaceAll("\\s+", "").replace("Á", "A");
    }

    private LocalDateTime calcularVencimientoHabilExistente(LocalDateTime inicio, Sede sede, int horasRestantes) {
        LocalDateTime fecha = inicio;
        long segundosRestantes = Duration.ofHours(horasRestantes).getSeconds();
        while (segundosRestantes > 0) {
            fecha = fecha.plusSeconds(60);
            if (esHoraHabil(fecha, sede)) segundosRestantes -= 60;
        }
        return fecha;
    }

    private boolean esHoraHabil(LocalDateTime fecha, Sede sede) {
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) return false;
        LocalTime hora = fecha.toLocalTime();
        boolean enManana = hora.isAfter(sede.getHoraInicioManana()) && hora.isBefore(sede.getHoraFinManana());
        boolean enTarde = hora.isAfter(sede.getHoraInicioTarde()) && hora.isBefore(sede.getHoraFinTarde());
        return enManana || enTarde;
    }
}
