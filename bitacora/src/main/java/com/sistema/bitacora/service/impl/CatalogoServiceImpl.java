package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.catalogo.CatalogoResponse;
import com.sistema.bitacora.repository.EstadoCasoRepository;
import com.sistema.bitacora.repository.SedeRepository;
import com.sistema.bitacora.repository.ServicioRepository;
import com.sistema.bitacora.repository.TipoServicioRepository;
import com.sistema.bitacora.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private final ServicioRepository servicioRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EstadoCasoRepository estadoCasoRepository;
    private final SedeRepository sedeRepository;

    @Override
    public List<CatalogoResponse> listarServicios() {
        return servicioRepository.findAll()
                .stream()
                .map(servicio -> new CatalogoResponse(
                        servicio.getId(),
                        servicio.getNombre()
                ))
                .toList();
    }

    @Override
    public List<CatalogoResponse> listarTiposServicio() {
        return tipoServicioRepository.findAll()
                .stream()
                .map(tipo -> new CatalogoResponse(
                        tipo.getId(),
                        tipo.getNombre()
                ))
                .toList();
    }

    @Override
    public List<CatalogoResponse> listarEstados() {
        return estadoCasoRepository.findAll()
                .stream()
                .map(estado -> new CatalogoResponse(
                        estado.getId(),
                        estado.getNombre()
                ))
                .toList();
    }

    @Override
    public List<CatalogoResponse> listarSedes() {
        return sedeRepository.findAll()
                .stream()
                .map(sede -> new CatalogoResponse(
                        sede.getId(),
                        sede.getNombreSede()
                ))
                .toList();
    }
}