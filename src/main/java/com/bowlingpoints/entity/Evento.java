package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;


@Data
@Entity
@Table(name = "evento")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private int idEvento;

    @Column(name = "nombre_evento")
    private String nombreEvento;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "id_tipo_evento",insertable=false, updatable=false)
    private int idTipoEvento;

    @Column(name = "estado_evento",insertable=false, updatable=false)
    private int idEstadoEvento;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "id_tipo_evento", referencedColumnName = "tipo_evento")
    private TipoEvento tipoEvento;

    @ManyToOne
    @JoinColumn(name = "estado_evento", referencedColumnName = "id_tipo_estado_evento")
    private EstadoEvento estadoEvento;
}
