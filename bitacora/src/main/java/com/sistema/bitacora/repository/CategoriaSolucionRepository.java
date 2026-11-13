package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.CategoriaSolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaSolucionRepository extends JpaRepository<CategoriaSolucion, Long> {
    List<CategoriaSolucion> findByServicioIdOrderByNombreAsc(Long servicioId);
    boolean existsByServicioIdAndNombreIgnoreCase(Long servicioId, String nombre);
}