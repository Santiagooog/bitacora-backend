package com.sistema.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sedes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sban;

    @Column(nullable = false, length = 150)
    private String nombreSede;

    @Column(length = 100)
    private String municipio;

    @Column(length = 100)
    private String regional;

    @Column(length = 100)
    private String departamento;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String horario;

    @Column(length = 100)
    private String atencion;

    // ✅ Nuevo campo para coincidir con la BD
    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = true;   // valor por defecto true

    // No olvides getters y setters (generados por Lombok)
}