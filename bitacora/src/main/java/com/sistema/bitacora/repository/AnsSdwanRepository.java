package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.AnsSdwan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnsSdwanRepository extends JpaRepository<AnsSdwan, Long> {
    Optional<AnsSdwan> findBySban(String sban);
}