package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "registro")
public class Registry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private int idRegistro;

    @Column(name = "id_persona")
    private int idPersona;

    @Column(name = "id_evento")
    private int idEvento;

    @Column(name = "activo")
    private boolean activo;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @Column(name = "fecha_actualizacion")
    private Date fechaActualizacion;

    @Column(name = "usuario_actualizacion")
    private String usuarioActualizacion;
}
