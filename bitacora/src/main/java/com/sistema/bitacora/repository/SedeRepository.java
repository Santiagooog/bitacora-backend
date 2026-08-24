package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {

    // Buscar por SBAN exacto
    Optional<Sede> findBySban(String sban);

    // Buscar por SBAN o nombre de sede (ignorando mayúsculas/minúsculas)
    @Query("SELECT s FROM Sede s WHERE LOWER(s.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(s.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Sede> buscarPorSbanONombre(@Param("termino") String termino);
}