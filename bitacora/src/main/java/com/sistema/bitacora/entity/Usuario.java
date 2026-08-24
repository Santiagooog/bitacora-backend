package com.sistema.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Implementamos UserDetails para integrarla con Spring Security
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER para cargar el Rol inmediatamente al autenticar
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }

    // ==========================================
    // MÉTODOS REQUERIDOS POR USERDETAILS
    // ==========================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convierte el nombre de tu entidad Rol (ej. "ADMIN", "SOPORTE") en un Authority de Spring
        // Es una buena práctica anteponer "ROLE_" si vas a usar seguridad basada en roles (ej. "ROLE_ADMIN")
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.getNombre().toUpperCase()));
    }

    @Override
    public String getUsername() {
        // En tu caso, el identificador único para iniciar sesión será el correo
        return this.correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Cuenta no expirada
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Cuenta no bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credenciales no expiradas
    }

    @Override
    public boolean isEnabled() {
        // Spring Security usará tu campo 'activo' para permitir o denegar el ingreso
        return this.activo;
    }
}