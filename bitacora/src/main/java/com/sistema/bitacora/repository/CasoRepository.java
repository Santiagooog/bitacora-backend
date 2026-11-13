package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Caso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CasoRepository extends JpaRepository<Caso, Long> {

    boolean existsByNumeroCasoInt(String numeroCasoInt);

    Optional<Caso> findByNumeroCasoInt(String numeroCasoInt);


    @Query("SELECT c FROM Caso c WHERE " +
            "LOWER(c.sedePrincipal.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.sedePrincipal.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.numeroCasoInt) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.descripcionFalla) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<Caso> buscarCasosPaginado(@Param("termino") String termino, Pageable pageable);

    @Query("SELECT c FROM Caso c JOIN c.sedePrincipal s WHERE LOWER(s.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(s.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Caso> buscarPorSede(@Param("termino") String termino);

    // ✅ MÉTODO CORREGIDO para obtener el último número de caso
    @Query("SELECT MAX(c.numeroCasoInt) FROM Caso c WHERE c.servicio.nombre = :nombreServicio AND c.numeroCasoInt LIKE CONCAT(:prefix, '%')")
    String findUltimoNumeroCaso(@Param("nombreServicio") String nombreServicio, @Param("prefix") String prefix);

        @Query("SELECT c.estado.nombre, COUNT(c) FROM Caso c GROUP BY c.estado.nombre")
        List<Object[]> countByEstado();

        @Query("SELECT c.servicio.nombre, COUNT(c) FROM Caso c GROUP BY c.servicio.nombre")
        List<Object[]> countByServicio();

        @Query("SELECT c.sedePrincipal.nombreSede, COUNT(c) FROM Caso c GROUP BY c.sedePrincipal.nombreSede")
        List<Object[]> countBySede();

    @Query("SELECT c FROM Caso c WHERE " +
            "c.estado.nombre = 'SOLUCIONADO' AND " +
            "(LOWER(c.descripcionFalla) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(c.categoriaFalla.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
            "ORDER BY c.fechaSolucion DESC")
    List<Caso> buscarCasosSimilares(@Param("texto") String texto);

    @Query("SELECT c FROM Caso c WHERE " +
            "c.estado.nombre = 'SOLUCIONADO' AND " +
            "c.categoriaFalla.id = :categoriaFallaId " +
            "ORDER BY c.fechaSolucion DESC")
    List<Caso> buscarCasosPorCategoriaFalla(@Param("categoriaFallaId") Long categoriaFallaId);

    @Query("SELECT c FROM Caso c WHERE " +
            "(:termino IS NULL OR :termino = '' OR " +
            "LOWER(c.descripcionFalla) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.numeroCasoInt) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.sedePrincipal.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.sedePrincipal.sban) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "(:estadoId IS NULL OR c.estado.id = :estadoId) AND " +
            "(:servicioId IS NULL OR c.servicio.id = :servicioId) AND " +
            "(:sedeId IS NULL OR c.sedePrincipal.id = :sedeId) AND " +
            "(:fechaDesde IS NULL OR c.fechaSolicitud >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR c.fechaSolicitud <= :fechaHasta)")
    Page<Caso> buscarCasosConFiltros(
            @Param("termino") String termino,
            @Param("estadoId") Long estadoId,
            @Param("servicioId") Long servicioId,
            @Param("sedeId") Long sedeId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );

    @Query("SELECT c FROM Caso c WHERE " +
            "(:termino IS NULL OR :termino = '' OR " +
            "LOWER(c.descripcionFalla) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.numeroCasoInt) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.sedePrincipal.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.sedePrincipal.sban) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "(c.estado.nombre IN ('ABIERTO', 'ESCALADO')) AND " +
            "(:servicioId IS NULL OR c.servicio.id = :servicioId) AND " +
            "(:sedeId IS NULL OR c.sedePrincipal.id = :sedeId) AND " +
            "(:fechaDesde IS NULL OR c.fechaSolicitud >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR c.fechaSolicitud <= :fechaHasta)")
    Page<Caso> buscarCasosActivosConFiltros(
            @Param("termino") String termino,
            @Param("servicioId") Long servicioId,
            @Param("sedeId") Long sedeId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );

    @Query("SELECT c FROM Caso c WHERE c.servicio.id IN :servicioIds AND " +
            "(:termino IS NULL OR :termino = '' OR LOWER(c.descripcionFalla) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(c.numeroCasoInt) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.sedePrincipal.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.sedePrincipal.sban) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "(:estadoId IS NULL OR c.estado.id = :estadoId) AND (:servicioId IS NULL OR c.servicio.id = :servicioId) AND " +
            "(:sedeId IS NULL OR c.sedePrincipal.id = :sedeId) AND (:fechaDesde IS NULL OR c.fechaSolicitud >= :fechaDesde) AND (:fechaHasta IS NULL OR c.fechaSolicitud <= :fechaHasta)")
    Page<Caso> buscarCasosConFiltrosParaServicios(@Param("termino") String termino, @Param("estadoId") Long estadoId,
            @Param("servicioId") Long servicioId, @Param("sedeId") Long sedeId,
            @Param("fechaDesde") LocalDateTime fechaDesde, @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("servicioIds") List<Long> servicioIds, Pageable pageable);

    @Query("SELECT c FROM Caso c WHERE c.servicio.id IN :servicioIds AND (LOWER(c.sedePrincipal.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.sedePrincipal.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.numeroCasoInt) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.descripcionFalla) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<Caso> buscarCasosPaginadoParaServicios(@Param("termino") String termino, @Param("servicioIds") List<Long> servicioIds, Pageable pageable);

    @Query("SELECT c FROM Caso c WHERE c.servicio.id IN :servicioIds AND c.estado.nombre IN ('ABIERTO', 'ESCALADO') AND " +
            "(:termino IS NULL OR :termino = '' OR LOWER(c.descripcionFalla) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.numeroCasoInt) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.sedePrincipal.nombreSede) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(c.sedePrincipal.sban) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "(:servicioId IS NULL OR c.servicio.id = :servicioId) AND (:sedeId IS NULL OR c.sedePrincipal.id = :sedeId) AND (:fechaDesde IS NULL OR c.fechaSolicitud >= :fechaDesde) AND (:fechaHasta IS NULL OR c.fechaSolicitud <= :fechaHasta)")
    Page<Caso> buscarCasosActivosParaServicios(@Param("termino") String termino, @Param("servicioId") Long servicioId,
            @Param("sedeId") Long sedeId, @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta, @Param("servicioIds") List<Long> servicioIds, Pageable pageable);


}
