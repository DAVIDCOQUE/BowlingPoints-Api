package com.bowlingpoints.entity;

import javax.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "ronda")
public class Ronda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ronda")
    private int idRonda;

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

    @Column(name = "id_persona" ,insertable=false, updatable=false)
    private int idPersona;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    private Persona persona;

    @Column(name = "id_evento")
    private int idEvento;
}
