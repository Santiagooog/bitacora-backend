package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.EstadoCaso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoCasoRepository extends JpaRepository<EstadoCaso, Long> {
    Optional<EstadoCaso> findByNombre(String nombre);
}