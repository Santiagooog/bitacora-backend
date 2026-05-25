package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
}