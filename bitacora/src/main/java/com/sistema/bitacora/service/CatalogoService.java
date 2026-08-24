package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.catalogo.CatalogoResponse;
import com.sistema.bitacora.dto.catalogo.SedeCompletaResponse;
import com.sistema.bitacora.entity.Rol;

import java.util.List;

public interface CatalogoService {

    List<CatalogoResponse> listarServicios();
    List<CatalogoResponse> listarTiposServicio();
    List<CatalogoResponse> listarEstados();
    List<SedeCompletaResponse> listarSedes();
    List<Rol> listarRoles();
    List<SedeCompletaResponse> buscarSedes(String termino);
}