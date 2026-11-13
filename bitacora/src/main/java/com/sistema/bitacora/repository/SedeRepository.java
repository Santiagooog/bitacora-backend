package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Sede;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {

    Optional<Sede> findBySban(String sban);

    // ✅ Este es el que faltaba — restaurado para CatalogoServiceImpl
    @Query("SELECT s FROM Sede s WHERE " +
            "LOWER(s.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(s.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Sede> buscarPorSbanONombre(@Param("termino") String termino);

    // Para carga masiva (un solo SELECT de todos los sbans)
    @Query("SELECT s.sban FROM Sede s")
    List<String> findAllSbans();

    // Para listado paginado de SedesPage
    @Query("SELECT s FROM Sede s WHERE " +
            "LOWER(s.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(s.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(s.municipio) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(s.regional) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<Sede> buscarPorTermino(@Param("termino") String termino, Pageable pageable);

    @Query("SELECT s FROM Sede s WHERE s.regional = :regional")
    Page<Sede> findByRegional(@Param("regional") String regional, Pageable pageable);

    @Query("SELECT s FROM Sede s WHERE s.regional = :regional AND (" +
            "LOWER(s.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(s.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(s.municipio) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<Sede> buscarPorRegionalYTermino(@Param("regional") String regional,
                                         @Param("termino") String termino,
                                         Pageable pageable);

    @Query("SELECT DISTINCT s.regional FROM Sede s WHERE s.regional IS NOT NULL ORDER BY s.regional")
    List<String> findDistinctRegionales();
}
