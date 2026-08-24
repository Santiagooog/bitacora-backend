package com.sistema.bitacora.config;

import com.sistema.bitacora.entity.*;
import com.sistema.bitacora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final ServicioRepository servicioRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EstadoCasoRepository estadoCasoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository; // ✅ Agregado
    private final PasswordEncoder passwordEncoder;       // ✅ Agregado

    @Override
    public void run(String... args) {
        // ========== ROLES ==========
        if (rolRepository.count() == 0) {
            rolRepository.save(Rol.builder().nombre("ADMIN").descripcion("Administrador").build());
            rolRepository.save(Rol.builder().nombre("AGENTE").descripcion("Agente de soporte").build());
            rolRepository.save(Rol.builder().nombre("SUPERVISOR").descripcion("Supervisor").build());
        }

        // ========== SERVICIOS ==========
        if (servicioRepository.count() == 0) {
            servicioRepository.save(Servicio.builder().nombre("SDWAN").descripcion("Servicio SDWAN").build());
            servicioRepository.save(Servicio.builder().nombre("WIFI PUBLICO").descripcion("Servicio WiFi Público").build());
            servicioRepository.save(Servicio.builder().nombre("WIFI ADMINISTRATIVO").descripcion("Servicio WiFi Administrativo").build());
            servicioRepository.save(Servicio.builder().nombre("CONTROL DE ACCESOS").descripcion("Control de accesos").build());
        }

        // ========== TIPOS DE SERVICIO ==========
        if (tipoServicioRepository.count() == 0) {
            tipoServicioRepository.save(TipoServicio.builder().nombre("INCIDENTE").descripcion("Incidente").build());
            tipoServicioRepository.save(TipoServicio.builder().nombre("REQUERIMIENTO").descripcion("Requerimiento").build());
        }

        // ========== ESTADOS ==========
        if (estadoCasoRepository.count() == 0) {
            estadoCasoRepository.save(EstadoCaso.builder().nombre("EN PROCESO").descripcion("Caso en proceso").build());
            estadoCasoRepository.save(EstadoCaso.builder().nombre("ESCALADO").descripcion("Caso escalado").build());
            estadoCasoRepository.save(EstadoCaso.builder().nombre("SOLUCIONADO").descripcion("Caso solucionado").build());
            estadoCasoRepository.save(EstadoCaso.builder().nombre("DEVUELTO").descripcion("Caso devuelto").build());
            estadoCasoRepository.save(EstadoCaso.builder().nombre("CANCELADO").descripcion("Caso cancelado").build());
        }


        // ========== USUARIO ADMIN POR DEFECTO ==========
        if (usuarioRepository.findByCorreo("admin@bitacora.com").isEmpty()) {
            Rol adminRol = rolRepository.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

            usuarioRepository.save(Usuario.builder()
                    .nombre("Administrador")
                    .apellido("Sistema")
                    .correo("admin@bitacora.com")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(adminRol)
                    .activo(true)
                    .build());
        }
    }
}