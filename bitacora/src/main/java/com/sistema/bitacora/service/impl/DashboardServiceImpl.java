package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.caso.DashboardResponse;
import com.sistema.bitacora.dto.caso.DashboardService;
import com.sistema.bitacora.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CasoRepository casoRepository;

    @Override
    public DashboardResponse obtenerEstadisticas() {
        long total = casoRepository.count();

        Map<String, Long> porEstado = casoRepository.countByEstado()
                .stream()
                .collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));

        Map<String, Long> porServicio = casoRepository.countByServicio()
                .stream()
                .collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));

        Map<String, Long> porSede = casoRepository.countBySede()
                .stream()
                .collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));

        return DashboardResponse.builder()
                .totalCasos(total)
                .casosPorEstado(porEstado)
                .casosPorServicio(porServicio)
                .casosPorSede(porSede)
                .build();
    }
}
