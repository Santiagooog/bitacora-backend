package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.HistorialCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialCasoRepository extends JpaRepository<HistorialCaso, Long> {
    List<HistorialCaso> findByCasoIdOrderByFechaDesc(Long casoId);
    void deleteByCasoId(Long casoId);
}