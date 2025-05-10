package com.bowlingpoints.entity;

import javax.persistence.*;
import lombok.Data;

import java.util.Date;


@Data
@Entity
@Table(name = "detalle_ronda")
public class DetailsRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_ronda")
    private int idDetalleRonda;

    @Column(name = "id_ronda")
    private int idRonda;

    @Column(name = "puntacion")
    private int puntacion;

    @Column(name = "juez_verificador")
    private String juezVerificador;

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
