package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.PuntoAcceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PuntoAccesoRepository extends JpaRepository<PuntoAcceso, Long> {

    Optional<PuntoAcceso> findBySerie(String serie);

    List<PuntoAcceso> findByPlaca(String placa);

    // ✅ Carga masiva: traer todas las series existentes de una vez
    @Query("SELECT p.serie FROM PuntoAcceso p WHERE p.serie IS NOT NULL")
    List<String> findAllSeries();

    // ✅ Búsqueda paginada
    @Query("SELECT p FROM PuntoAcceso p WHERE " +
            "LOWER(p.serie) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.mac) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.placa) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.nombreAxiom) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.area) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<PuntoAcceso> buscarPorTermino(@Param("termino") String termino, Pageable pageable);

    // ✅ Filtro por sede
    @Query("SELECT p FROM PuntoAcceso p WHERE p.sban = :sban")
    Page<PuntoAcceso> findBySban(@Param("sban") String sban, Pageable pageable);

    // ✅ Filtro por tipo
    @Query("SELECT p FROM PuntoAcceso p WHERE p.tipo = :tipo")
    Page<PuntoAcceso> findByTipo(@Param("tipo") String tipo, Pageable pageable);

    // ✅ Filtro por regional
    @Query("SELECT p FROM PuntoAcceso p WHERE p.regional = :regional")
    Page<PuntoAcceso> findByRegional(@Param("regional") String regional, Pageable pageable);

    // ✅ Combinación: sede + tipo
    @Query("SELECT p FROM PuntoAcceso p WHERE p.sban = :sban AND p.tipo = :tipo")
    Page<PuntoAcceso> findBySbanAndTipo(@Param("sban") String sban,
                                        @Param("tipo") String tipo,
                                        Pageable pageable);

    // ✅ Para los dropdowns
    @Query("SELECT DISTINCT p.sban FROM PuntoAcceso p WHERE p.sban IS NOT NULL ORDER BY p.sban")
    List<String> findDistinctSbans();

    @Query("SELECT DISTINCT p.regional FROM PuntoAcceso p WHERE p.regional IS NOT NULL ORDER BY p.regional")
    List<String> findDistinctRegionales();

    @Query("SELECT DISTINCT p.tipo FROM PuntoAcceso p WHERE p.tipo IS NOT NULL ORDER BY p.tipo")
    List<String> findDistinctTipos();

    @Query("SELECT DISTINCT p.proyecto FROM PuntoAcceso p WHERE p.proyecto IS NOT NULL AND p.proyecto <> '' ORDER BY p.proyecto")
    List<String> findDistinctProyectos();

    @Query("SELECT p FROM PuntoAcceso p WHERE " +
            "(:termino = '' OR LOWER(COALESCE(p.serie, '')) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(COALESCE(p.mac, '')) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(COALESCE(p.placa, '')) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(COALESCE(p.nombreAxiom, '')) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(COALESCE(p.area, '')) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "(:sban IS NULL OR p.sban = :sban) AND " +
            "(:tipo IS NULL OR p.tipo = :tipo) AND " +
            "(:regional IS NULL OR p.regional = :regional) AND " +
            "(:proyecto IS NULL OR p.proyecto = :proyecto OR " +
            "(:proyecto = 'CAS 1528' AND UPPER(COALESCE(p.proyecto, '')) IN ('S1528', 'CAS_1528')) OR " +
            "(:proyecto = 'CAS 1528A' AND UPPER(COALESCE(p.proyecto, '')) IN ('S1528A', 'CAS_1528A')))")
    Page<PuntoAcceso> buscarConFiltros(@Param("termino") String termino,
                                       @Param("sban") String sban,
                                       @Param("tipo") String tipo,
                                       @Param("regional") String regional,
                                       @Param("proyecto") String proyecto,
                                       Pageable pageable);

    // ✅ Conteo por sede (reporte rápido)
    @Query("SELECT p.sban, COUNT(p) FROM PuntoAcceso p GROUP BY p.sban ORDER BY p.sban")
    List<Object[]> contarPorSban();
}
