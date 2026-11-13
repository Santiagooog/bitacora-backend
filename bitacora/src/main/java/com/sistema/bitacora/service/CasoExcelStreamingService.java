package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.dto.historial.HistorialCasoResponse;
import com.sistema.bitacora.entity.Caso;
import com.sistema.bitacora.mapper.CasoMapper;
import com.sistema.bitacora.mapper.HistorialCasoMapper;
import com.sistema.bitacora.repository.CasoRepository;
import com.sistema.bitacora.repository.HistorialCasoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CasoExcelStreamingService {

    private static final int TAMANO_BLOQUE = 200;
    private static final int LIMITE_CELDA = 32_767;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] COLUMNAS_CASO = {
            "ID", "Número Caso", "Número plataforma", "Fecha solicitud", "Servicio", "Tipo servicio",
            "Estado", "Sede", "SBAN", "Proyecto", "Descripción falla", "Categoría falla",
            "Categoría solución", "Observaciones", "Canal escalamiento", "Grupo escalamiento", "Escalado a",
            "Equipo", "Modelo", "Serial", "Placa", "Creado por", "Actualizado por", "Fecha vencimiento",
            "Fecha solución", "Fecha creación", "Fecha actualización"
    };
    private static final String[] COLUMNAS_HISTORIAL = {
            "ID Caso", "Número Caso", "Fecha", "Acción", "Campo modificado", "Valor anterior",
            "Valor nuevo", "Usuario", "Observación"
    };

    private final CasoRepository casoRepository;
    private final HistorialCasoRepository historialCasoRepository;
    private final CasoMapper casoMapper;
    private final HistorialCasoMapper historialCasoMapper;

    @Transactional(readOnly = true)
    public void escribirExcel(String termino, Long estadoId, Long servicioId, Long sedeId,
                              LocalDateTime fechaDesde, LocalDateTime fechaHasta, boolean soloActivos,
                              List<Long> serviciosPermitidos, OutputStream salida) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        try {
            Sheet hojaCasos = workbook.createSheet("Casos");
            Sheet hojaHistorial = workbook.createSheet("Trazabilidad");
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            crearEncabezado(hojaCasos, COLUMNAS_CASO, estiloEncabezado);
            crearEncabezado(hojaHistorial, COLUMNAS_HISTORIAL, estiloEncabezado);
            hojaCasos.createFreezePane(0, 1);
            hojaHistorial.createFreezePane(0, 1);

            int filaCaso = 1;
            int filaHistorial = 1;
            int paginaActual = 0;
            Page<Caso> pagina;
            do {
                Pageable pageable = PageRequest.of(paginaActual, TAMANO_BLOQUE,
                        Sort.by(Sort.Direction.DESC, "fechaSolicitud"));
                if (serviciosPermitidos != null && serviciosPermitidos.isEmpty()) {
                    pagina = Page.empty(pageable);
                } else if (soloActivos && estadoId == null) {
                    pagina = serviciosPermitidos == null
                            ? casoRepository.buscarCasosActivosConFiltros(termino, servicioId, sedeId, fechaDesde, fechaHasta, pageable)
                            : casoRepository.buscarCasosActivosParaServicios(termino, servicioId, sedeId, fechaDesde, fechaHasta, serviciosPermitidos, pageable);
                } else {
                    pagina = serviciosPermitidos == null
                            ? casoRepository.buscarCasosConFiltros(termino, estadoId, servicioId, sedeId, fechaDesde, fechaHasta, pageable)
                            : casoRepository.buscarCasosConFiltrosParaServicios(termino, estadoId, servicioId, sedeId, fechaDesde, fechaHasta, serviciosPermitidos, pageable);
                }

                List<Caso> casos = pagina.getContent();
                Map<Long, String> numeroPorId = new HashMap<>();
                for (Caso caso : casos) {
                    CasoResponse dto = casoMapper.toResponse(caso);
                    numeroPorId.put(dto.getId(), dto.getNumeroCasoInt());
                    escribirFila(hojaCasos, filaCaso++, valoresCaso(dto));
                }

                if (!casos.isEmpty()) {
                    List<Long> ids = casos.stream().map(Caso::getId).toList();
                    List<HistorialCasoResponse> eventos = historialCasoRepository
                            .findByCasoIdInOrderByFechaDesc(ids)
                            .stream().map(historialCasoMapper::toResponse).toList();
                    for (HistorialCasoResponse evento : eventos) {
                        escribirFila(hojaHistorial, filaHistorial++, valoresHistorial(evento,
                                numeroPorId.get(evento.getCasoId())));
                    }
                }
                paginaActual++;
            } while (!pagina.isLast());

            workbook.write(salida);
            salida.flush();
        } finally {
            try {
                workbook.close();
            } finally {
                workbook.dispose();
            }
        }
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }

    private void crearEncabezado(Sheet hoja, String[] columnas, CellStyle estilo) {
        Row fila = hoja.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estilo);
        }
    }

    private List<String> valoresCaso(CasoResponse caso) {
        return List.of(texto(caso.getId()), texto(caso.getNumeroCasoInt()), texto(caso.getNumeroCasoESolution()),
                fecha(caso.getFechaSolicitud()), texto(caso.getServicio()), texto(caso.getTipoServicio()),
                texto(caso.getEstado()), texto(caso.getSede()), texto(caso.getSban()), texto(caso.getProyecto()),
                texto(caso.getDescripcionFalla()), texto(caso.getCategoriaFallaNombre()),
                texto(caso.getCategoriaSolucionNombre()), texto(caso.getObservaciones()),
                texto(caso.getCanalEscalamiento()), texto(caso.getGrupoEscalamiento()), texto(caso.getEscaladoA()),
                primero(caso.getEquipo(), caso.getActivoName()), primero(caso.getModelo(), caso.getActivoDeviceType()),
                primero(caso.getSerial(), caso.getActivoSerial()), primero(caso.getPlaca(), caso.getActivoPlaca()),
                unir(caso.getCreadoPorNombre(), caso.getCreadoPorApellido()),
                unir(caso.getActualizadoPorNombre(), caso.getActualizadoPorApellido()),
                fecha(caso.getFechaVencimiento()), fecha(caso.getFechaSolucion()), fecha(caso.getFechaCreacion()),
                fecha(caso.getFechaActualizacion()));
    }

    private List<String> valoresHistorial(HistorialCasoResponse evento, String numeroCaso) {
        return List.of(texto(evento.getCasoId()), texto(numeroCaso), fecha(evento.getFecha()), texto(evento.getAccion()),
                texto(evento.getCampoModificado()), texto(evento.getValorAnterior()), texto(evento.getValorNuevo()),
                unir(evento.getNombreUsuario(), evento.getApellidoUsuario()), texto(evento.getObservacion()));
    }

    private void escribirFila(Sheet hoja, int indice, List<String> valores) {
        Row fila = hoja.createRow(indice);
        for (int i = 0; i < valores.size(); i++) {
            String valor = valores.get(i);
            if (valor.length() > LIMITE_CELDA) valor = valor.substring(0, LIMITE_CELDA);
            fila.createCell(i).setCellValue(valor);
        }
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private String fecha(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    private String primero(String valor, String alternativo) {
        return valor == null || valor.isBlank() ? texto(alternativo) : valor;
    }

    private String unir(String nombre, String apellido) {
        return List.of(texto(nombre), texto(apellido)).stream().filter(valor -> !valor.isBlank())
                .reduce((a, b) -> a + " " + b).orElse("");
    }
}
