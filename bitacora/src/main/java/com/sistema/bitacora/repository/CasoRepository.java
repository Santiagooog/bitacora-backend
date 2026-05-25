package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Caso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CasoRepository extends JpaRepository<Caso, Long> {
    boolean existsByNumeroCasoInt(String numeroCasoInt);
    Optional<Caso> findByNumeroCasoInt(String numeroCasoInt);
}