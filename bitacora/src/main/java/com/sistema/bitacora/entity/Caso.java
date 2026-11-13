package com.sistema.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "casos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String equipo;

    private String proyecto;

    // En Caso.java
    @Column(name = "grupo_escalamiento", length = 100)
    private String grupoEscalamiento;

    @Column(name = "escalado_a", length = 100)
    private String escaladoA; // Aquí irá Binarity o Enertecol


    @Column(length = 100)
    private String modelo;

    @Column(length = 100)
    private String serial;

    @Column(length = 100)
    private String placa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activo_id")
    private Activo activo;
    @Column(name = "canal_escalamiento", length = 50)
    private String canalEscalamiento;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id")
    private Usuario creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por_id")
    private Usuario actualizadoPor;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    @Column(nullable = false, unique = true, length = 100)
    private String numeroCasoInt;

    @Column(length = 100)
    private String numeroCasoESolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoCaso estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sedePrincipal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcionFalla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_recibe_id")
    private Usuario agenteRecibe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_resolutor_id")
    private Usuario agenteResolutor;

    private LocalDateTime fechaSolucion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_falla_id")
    private CategoriaFalla categoriaFalla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_solucion_id")
    private CategoriaSolucion categoriaSolucion;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.fechaCreacion = now;
        this.fechaActualizacion = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}