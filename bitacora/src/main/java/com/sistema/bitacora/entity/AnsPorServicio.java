package com.sistema.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ans_por_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnsPorServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_oficinas", length = 20)
    private String sban;

    @Column(name = "oficinas_tipo", length = 50)
    private String oficinasTipo;

    @Column(length = 100)
    private String reg;

    @Column(length = 255)
    private String oficina;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String horario;

    @Column(name = "tiempo_sdwan")
    private Integer tiempoSdwan;

    @Column(name = "tiempo_wifi_publico")
    private Integer tiempoWifiPublico;

    @Column(name = "tiempo_control_acceso")
    private Integer tiempoControlAcceso;
}