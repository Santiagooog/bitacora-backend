package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.catalogo.CatalogoResponse;

import java.util.List;

public interface CatalogoService {

    List<CatalogoResponse> listarServicios();
    List<CatalogoResponse> listarTiposServicio();
    List<CatalogoResponse> listarEstados();
    List<CatalogoResponse> listarSedes();
}