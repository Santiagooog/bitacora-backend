package com.sistema.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String serial;

    private String proyecto; // Ej: "A", "B", "General"

    @Column(length = 200)
    private String name;

    @Column(length = 20)
    private String sban;

    @Column(length = 200)
    private String site;

    @Column(name = "device_type", length = 100)
    private String deviceType;

    @Column(length = 100)
    private String placa;

    // ✅ Nuevo: tipo de servicio (SDWAN, WIFI, ACCESOS)
    @Column(name = "tipo_servicio", length = 100, nullable = false)
    private String tipoServicio;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
