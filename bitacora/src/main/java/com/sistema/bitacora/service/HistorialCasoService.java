package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.historial.HistorialCasoResponse;
import com.sistema.bitacora.entity.HistorialCaso;
import com.sistema.bitacora.entity.Usuario;
import com.sistema.bitacora.mapper.HistorialCasoMapper;
import com.sistema.bitacora.repository.HistorialCasoRepository;
import com.sistema.bitacora.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistorialCasoService {

    private final HistorialCasoRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialCasoMapper historialMapper;

    public void registrar(Long casoId, String accion, String campo, String anterior, String nuevo, String observacion) {
        Usuario usuario = obtenerUsuarioAutenticado();

        HistorialCaso historial = HistorialCaso.builder()
                .casoId(casoId)
                .usuario(usuario)
                .accion(accion)
                .campoModificado(campo)
                .valorAnterior(anterior)
                .valorNuevo(nuevo)
                .observacion(observacion)
                .build();

        historialRepository.save(historial);
    }

    public List<HistorialCasoResponse> listarPorCaso(Long casoId) {
        return historialRepository.findByCasoIdOrderByFechaDesc(casoId)
                .stream()
                .map(historialMapper::toResponse)
                .toList();
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            return usuarioRepository.findByCorreo(username).orElse(null);
        }
        return null;
    }
}