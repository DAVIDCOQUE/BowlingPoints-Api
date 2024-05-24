package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;


@Data
@Entity
@Table(name = "evento")
public class Event {
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

    @ManyToOne
    @JoinColumn(name = "id_tipo_evento", referencedColumnName = "tipo_evento")
    private TypeEvent tipoEvento;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "user_id")
    private User usuario;


}
