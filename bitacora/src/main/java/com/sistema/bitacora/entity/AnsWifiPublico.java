package com.sistema.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ans_wifi_publico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnsWifiPublico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String sban;

    @Column(name = "cod_dane", length = 10)
    private String codDane;

    @Column(length = 150)
    private String oficina;

    @Column(length = 100)
    private String municipio;

    @Column(length = 100)
    private String departamento;

    @Column(length = 10)
    private String tipo;

    @Column(length = 255)
    private String direccion;

    @Column(name = "tiempo_solucion")
    private Integer tiempoSolucion;

    @Column(name = "cantidad_aps")
    private Integer cantidadAps;
}