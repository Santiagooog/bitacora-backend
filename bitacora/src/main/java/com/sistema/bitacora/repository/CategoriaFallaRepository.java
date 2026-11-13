package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.CategoriaFalla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaFallaRepository extends JpaRepository<CategoriaFalla, Long> {
    List<CategoriaFalla> findByServicioIdOrderByNombreAsc(Long servicioId);
    boolean existsByServicioIdAndNombreIgnoreCase(Long servicioId, String nombre);
}