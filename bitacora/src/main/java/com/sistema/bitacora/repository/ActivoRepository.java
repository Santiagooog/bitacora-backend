package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Activo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivoRepository extends JpaRepository<Activo, Long> {

    Optional<Activo> findBySerial(String serial);
    List<Activo> findBySban(String sban);
    List<Activo> findBySbanAndTipoServicio(String sban, String tipoServicio);

    @Query("SELECT DISTINCT a.sban FROM Activo a WHERE a.tipoServicio IN :tiposServicio AND a.activo = true AND a.sban IS NOT NULL")
    List<String> findSbansByTipoServicioInAndActivoTrue(@Param("tiposServicio") List<String> tiposServicio);

    // ✅ Nuevos
    Page<Activo> findByTipoServicio(String tipoServicio, Pageable pageable);

    @Query("SELECT a FROM Activo a WHERE " +
            "LOWER(a.serial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.site) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<Activo> buscarPorTermino(@Param("termino") String termino, Pageable pageable);

    @Query("SELECT a.serial FROM Activo a")
    List<String> findAllSeriales();

    @Query("SELECT a FROM Activo a WHERE a.tipoServicio = :tipoServicio AND (" +
            "LOWER(a.serial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.sban) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(a.site) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<Activo> buscarPorTipoYTermino(@Param("tipoServicio") String tipoServicio,
                                       @Param("termino") String termino,
                                       Pageable pageable);
}