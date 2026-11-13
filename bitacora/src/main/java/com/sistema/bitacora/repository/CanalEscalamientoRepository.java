package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.CanalEscalamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CanalEscalamientoRepository extends JpaRepository<CanalEscalamiento, Long> {
    List<CanalEscalamiento> findByActivoTrueOrderByNombreAsc();
    Optional<CanalEscalamiento> findByNombreIgnoreCase(String nombre);
}