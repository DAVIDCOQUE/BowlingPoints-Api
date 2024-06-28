package com.bowlingpoints.entity;


import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "tipo_estado_evento")
public class EstadoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_estado_evento")
    private int idTipoEstadoEvento;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "activo")
    private boolean activo;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "usuario_creacion")
    private String usuario_creacion;

    @Column(name = "fecha_actualizacion")
    private Date fechaActualizacion;

}
