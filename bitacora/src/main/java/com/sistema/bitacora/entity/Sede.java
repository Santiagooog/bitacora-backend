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

    private String sban;

    private String nombreSede;

    private String municipio;

    private String regional;

    private String departamento;

    private String direccion;

    private String horario;

    private String enlaceTelefonica;

    private String medioEnlace;

    private Boolean activa;
}