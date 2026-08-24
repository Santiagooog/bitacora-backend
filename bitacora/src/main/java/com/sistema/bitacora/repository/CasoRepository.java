package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Caso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}