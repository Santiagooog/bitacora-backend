package com.sistema.bitacora.config;

import com.sistema.bitacora.entity.CategoriaFalla;
import com.sistema.bitacora.entity.CategoriaSolucion;
import com.sistema.bitacora.entity.Servicio;
import com.sistema.bitacora.repository.CategoriaFallaRepository;
import com.sistema.bitacora.repository.CategoriaSolucionRepository;
import com.sistema.bitacora.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CargaCategoriasIniciales implements CommandLineRunner {

    private final CategoriaFallaRepository fallaRepository;
    private final CategoriaSolucionRepository solucionRepository;
    private final ServicioRepository servicioRepository;

    @Override
    public void run(String... args) throws Exception {
        if (fallaRepository.count() > 0 || solucionRepository.count() > 0) {
            System.out.println("Categorías ya cargadas, omitiendo...");
            return;
        }

        Servicio wifi = servicioRepository.findByNombreIgnoreCase("WIFI PUBLICO")
                .orElseGet(() -> servicioRepository.save(Servicio.builder()
                        .nombre("WIFI PUBLICO")
                        .descripcion("WiFi Público")
                        .build()));

        // Cargar fallas
        List<String> fallas = List.of(
                "AP Offline", "AR Offline", "Cambio WAN", "Autenticación MAC",
                "Conexión Fisica", "Falla Conexión SSID", "Falla portal",
                "Monitoreo de Sede", "Sin Navegación", "Falla Imaster", "Falla Datawifi"
        );

        for (String nombre : fallas) {
            fallaRepository.save(CategoriaFalla.builder()
                    .servicio(wifi)
                    .nombre(nombre)
                    .activo(true)
                    .build());
        }

        // Cargar soluciones
        List<String> soluciones = List.of(
                "Canal de Datos", "Ajuste configuración Portal Cautivo", "Ap Dañado",
                "Registro MAC", "Bloqueo AP", "Daño Cableado", "Cambio WAN",
                "Manipulacion indebida", "AR Dañado", "POE Dañado", "Patch Cord Dañado",
                "MTU", "Normalizado sin intervencion", "Ajuste NCE Imaster, Datawifi, Smartfilter",
                "Desconocimiento del usuario", "Falla Masiva Canal", "Reconfiguracion Router",
                "Cambio interfaz", "Calamidad doméstica"
        );

        for (String nombre : soluciones) {
            solucionRepository.save(CategoriaSolucion.builder()
                    .servicio(wifi)
                    .nombre(nombre)
                    .activo(true)
                    .build());
        }

        System.out.println("Categorías cargadas correctamente.");
    }
}