package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.AnsPorServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnsPorServicioRepository extends JpaRepository<AnsPorServicio, Long> {
    Optional<AnsPorServicio> findBySban(String sban);


    @Query("SELECT a FROM AnsPorServicio a WHERE " +
            "LOWER(a.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.oficina) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.ciudad) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<AnsPorServicio> buscarPorTermino(@Param("termino") String termino, Pageable pageable);
}