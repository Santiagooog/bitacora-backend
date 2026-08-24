package com.sistema.bitacora.repository;

import com.sistema.bitacora.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método clave para buscar al usuario por su correo al momento de hacer Login
    Optional<Usuario> findByCorreo(String correo);
}