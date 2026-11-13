package com.sistema.bitacora.config;

import com.sistema.bitacora.entity.*;
import com.sistema.bitacora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final CanalEscalamientoRepository canalEscalamientoRepository;
    private final CategoriaFallaRepository categoriaFallaRepository;
    private final CategoriaSolucionRepository categoriaSolucionRepository;

    @Value("${app.initial-admin.password:}")
    private String initialAdminPassword;

    @Value("${BITACORA_ADMIN_RESET_PASSWORD:}")
    private String adminResetPassword;


    @Override
    @Transactional
    public void run(String... args) {
        // ========== ROLES ==========
        if (rolRepository.count() == 0) {
            rolRepository.save(Rol.builder().nombre("ADMIN").descripcion("Administrador").build());
            rolRepository.save(Rol.builder().nombre("AGENTE").descripcion("Agente de soporte").build());
            rolRepository.save(Rol.builder().nombre("SUPERVISOR").descripcion("Supervisor").build());
        }

        // ========== SERVICIOS ==========
        asegurarServicio("SDWAN", "Servicio SDWAN");
        asegurarServicio("WIFI PUBLICO", "Servicio WiFi Público");
        asegurarServicio("WIFI ADMINISTRATIVO", "Servicio WiFi Administrativo");
        Servicio cas1528 = asegurarServicio("CAS 1528", "Proyecto CAS 1528");
        Servicio cas1528A = asegurarServicio("CAS 1528A", "Proyecto CAS 1528A");

        // Los usuarios existentes conservan acceso inicial a los servicios actuales.
        // Los nuevos usuarios reciben permisos explícitos al ser creados.
        usuarioRepository.findAll().stream()
                .filter(usuario -> !"ADMIN".equalsIgnoreCase(usuario.getRol().getNombre()))
                .filter(usuario -> usuario.getServiciosPermitidos().isEmpty())
                .forEach(usuario -> {
                    usuario.setServiciosPermitidos(new java.util.HashSet<>(servicioRepository.findByActivoTrue()));
                    usuarioRepository.save(usuario);
                });

        // Se conserva el registro antiguo para los casos históricos, pero ya no se ofrece al crear casos.
        servicioRepository.findByNombreIgnoreCase("CONTROL DE ACCESOS").ifPresent(servicioAntiguo -> {
            copiarCategoriasAntiguas(servicioAntiguo, cas1528);
            copiarCategoriasAntiguas(servicioAntiguo, cas1528A);
            servicioAntiguo.setActivo(false);
            servicioRepository.save(servicioAntiguo);
        });

        // ========== TIPOS DE SERVICIO ==========
        if (tipoServicioRepository.count() == 0) {
            tipoServicioRepository.save(TipoServicio.builder().nombre("INCIDENTE").descripcion("Incidente").build());
            tipoServicioRepository.save(TipoServicio.builder().nombre("REQUERIMIENTO").descripcion("Requerimiento").build());
        }


        // ========== CANALES DE ESCALAMIENTO ==========
        String[][] canalesData = {
                {"E-SOLUTIONS", "Escalamiento por E-Solutions"},
                {"WHATSAPP", "Escalamiento por WhatsApp"},
                {"CORREO", "Escalamiento por Correo"},
                {"ARANDA", "Escalamiento por Aranda"},
                {"LLAMADA", "Escalamiento por Llamada"}
        };

        for (String[] canal : canalesData) {
            if (canalEscalamientoRepository.findByNombreIgnoreCase(canal[0]).isEmpty()) {
                canalEscalamientoRepository.save(CanalEscalamiento.builder()
                        .nombre(canal[0])
                        .descripcion(canal[1])
                        .activo(true)
                        .build());
            }
        }
        String[][] estadosData = {
                {"ABIERTO", "Caso abierto"},
                {"EN PROCESO", "Caso en proceso"},
                {"ESCALADO", "Caso escalado"},
                {"SOPORTE EN SITIO", "Soporte en sitio"},
                {"FABRICANTE", "Enviado al fabricante"},
                {"ESPECIALISTAS", "Con especialistas"},
                {"SOLUCIONADO", "Caso solucionado"},
                {"DEVUELTO", "Caso devuelto"},
                {"CANCELADO", "Caso cancelado"},
                {"CERRADO", "Caso cerrado"}
        };

        for (String[] estado : estadosData) {
            if (estadoCasoRepository.findByNombre(estado[0]).isEmpty()) {
                estadoCasoRepository.save(EstadoCaso.builder()
                        .nombre(estado[0])
                        .descripcion(estado[1])
                        .build());
            }
        }


        // ========== USUARIO ADMIN POR DEFECTO / RESTABLECIMIENTO LOCAL ==========
        var adminExistente = usuarioRepository.findByCorreo("admin@bitacora.com");
        if (adminExistente.isPresent() && adminResetPassword != null && !adminResetPassword.isBlank()) {
            var administrador = adminExistente.get();
            administrador.setPassword(passwordEncoder.encode(adminResetPassword));
            usuarioRepository.save(administrador);
            System.out.println("Contraseña del administrador restablecida desde la configuración local.");
        } else if (adminExistente.isEmpty() && initialAdminPassword != null && !initialAdminPassword.isBlank()) {
            Rol adminRol = rolRepository.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

            usuarioRepository.save(Usuario.builder()
                    .nombre("Administrador")
                    .apellido("Sistema")
                    .correo("admin@bitacora.com")
                    .password(passwordEncoder.encode(initialAdminPassword))
                    .rol(adminRol)
                    .activo(true)
                    .build());
        }
    }

    private Servicio asegurarServicio(String nombre, String descripcion) {
        Servicio servicio = servicioRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> Servicio.builder().nombre(nombre).descripcion(descripcion).activo(true).build());
        servicio.setActivo(true);
        return servicioRepository.save(servicio);
    }

    private void copiarCategoriasAntiguas(Servicio origen, Servicio destino) {
        categoriaFallaRepository.findByServicioIdOrderByNombreAsc(origen.getId()).forEach(categoria -> {
            if (!categoriaFallaRepository.existsByServicioIdAndNombreIgnoreCase(destino.getId(), categoria.getNombre())) {
                categoriaFallaRepository.save(CategoriaFalla.builder()
                        .servicio(destino)
                        .nombre(categoria.getNombre())
                        .activo(categoria.getActivo())
                        .build());
            }
        });
        categoriaSolucionRepository.findByServicioIdOrderByNombreAsc(origen.getId()).forEach(categoria -> {
            if (!categoriaSolucionRepository.existsByServicioIdAndNombreIgnoreCase(destino.getId(), categoria.getNombre())) {
                categoriaSolucionRepository.save(CategoriaSolucion.builder()
                        .servicio(destino)
                        .nombre(categoria.getNombre())
                        .activo(categoria.getActivo())
                        .build());
            }
        });
    }
}
