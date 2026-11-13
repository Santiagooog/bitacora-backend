package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.AnsSdwan;
import com.sistema.bitacora.entity.AnsWifiPublico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnsWifiPublicoRepository extends JpaRepository<AnsWifiPublico, Long> {
    Optional<AnsWifiPublico> findBySban(String sban);
}