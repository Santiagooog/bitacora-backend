package com.sistema.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "puntos_acceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Clave lógica: la PLACA agrupa los 3 dispositivos (controladora + 2 lectoras)
    @Column(name = "placa", length = 50)
    private String placa;

    @Column(name = "estado", length = 50)
    private String estado;

    @Column(name = "marca", length = 50)
    private String marca;

    // CONTROLADORA | LECTORA
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    @Column(name = "modelo", length = 150)
    private String modelo;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    // ✅ Identificador físico único por dispositivo
    @Column(name = "serie", length = 100, unique = true)
    private String serie;

    @Column(name = "mac", length = 50)
    private String mac;

    @Column(name = "sban", length = 20)
    private String sban;

    @Column(name = "oficina", length = 200)
    private String oficina;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "regional", length = 100)
    private String regional;

    @Column(name = "area", length = 200)
    private String area;

    @Column(name = "piso")
    private Integer piso;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    @Column(name = "fecha_inicio_soporte")
    private LocalDate fechaInicioSoporte;

    @Column(name = "fecha_fin_soporte")
    private LocalDate fechaFinSoporte;

    @Column(name = "nombre_funcionario", length = 200)
    private String nombreFuncionario;

    @Column(name = "cedula", length = 50)
    private String cedula;

    @Column(name = "cargo", length = 200)
    private String cargo;

    @Column(name = "nombre_axiom", length = 200)
    private String nombreAxiom;

    @Column(name = "fecha_mantenimiento1", length = 100)
    private String fechaMantenimiento1;

    @Column(name = "fecha_mantenimiento2", length = 100)
    private String fechaMantenimiento2;

    @Column(name = "version_firmware", length = 50)
    private String versionFirmware;

    @Column(name = "proyecto", length = 50)
    private String proyecto;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}