package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.catalogo.CatalogoResponse;
import com.sistema.bitacora.dto.catalogo.SedeCompletaResponse;
import com.sistema.bitacora.entity.Rol;
import com.sistema.bitacora.entity.Sede;
import com.sistema.bitacora.entity.Servicio;
import com.sistema.bitacora.repository.*;
import com.sistema.bitacora.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private final ServicioRepository servicioRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EstadoCasoRepository estadoCasoRepository;
    private final SedeRepository sedeRepository;
    private final RolRepository rolRepository;


    @Override
    public List<CatalogoResponse> listarServicios() {
        return servicioRepository.findByActivoTrue()
                .stream()
                .map(servicio -> new CatalogoResponse(
                        servicio.getId(),
                        servicio.getNombre()
                ))
                .toList();
    }

    @Override
    public CatalogoResponse crearServicio(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del servicio es obligatorio.");
        }
        String nombreNormalizado = nombre.trim().replaceAll("\\s+", " ");
        if (nombreNormalizado.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede superar 100 caracteres.");
        }
        if (descripcion != null && descripcion.trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción no puede superar 255 caracteres.");
        }
        if (servicioRepository.findByNombreIgnoreCase(nombreNormalizado).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un servicio con ese nombre.");
        }

        String descripcionNormalizada = descripcion == null || descripcion.isBlank()
                ? null : descripcion.trim();
        Servicio guardado = servicioRepository.save(Servicio.builder()
                .nombre(nombreNormalizado)
                .descripcion(descripcionNormalizada)
                .activo(true)
                .build());
        return new CatalogoResponse(guardado.getId(), guardado.getNombre());
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
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
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
    public List<SedeCompletaResponse> buscarSedes(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarSedes(); // opcional: todas si no hay término
        }
        return sedeRepository.buscarPorSbanONombre(termino.trim())
                .stream()
                .map(this::toSedeCompletaResponse)   // usa tu conversión existente
                .toList();
    }

    // Si no tienes este método privado, crea uno que mapee Sede a SedeCompletaResponse
    private SedeCompletaResponse toSedeCompletaResponse(Sede sede) {
        return SedeCompletaResponse.builder()
                .id(sede.getId())
                .sban(sede.getSban())
                .nombreSede(sede.getNombreSede())
                .municipio(sede.getMunicipio())
                .regional(sede.getRegional())
                .departamento(sede.getDepartamento())
                .direccion(sede.getDireccion())
                .horario(sede.getHorario())
                .atencion(sede.getAtencion())
                .activa(sede.getActiva())
                .build();
    }

    @Override
    public List<SedeCompletaResponse> listarSedes() {
        return sedeRepository.findAll()
                .stream()
                .map(sede -> SedeCompletaResponse.builder()
                        .id(sede.getId())
                        .sban(sede.getSban())
                        .nombreSede(sede.getNombreSede())
                        .municipio(sede.getMunicipio())
                        .regional(sede.getRegional())       // ✅ ahora sí existe
                        .departamento(sede.getDepartamento())
                        .direccion(sede.getDireccion())
                        .horario(sede.getHorario())
                        .atencion(sede.getAtencion())       // ✅ ahora sí existe
                        .activa(sede.getActiva())           // ✅ ahora sí existe
                        .build())
                .toList();
    }
}
