package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    Optional<Servicio> findByNombreIgnoreCase(String nombre);
    java.util.List<Servicio> findByActivoTrue();

}
