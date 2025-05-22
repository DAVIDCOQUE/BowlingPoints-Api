package com.bowlingpoints.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "asistencia")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private int idAsistencia;

    @Column(name = "id_evento_hora")
    private int idEventoHora;

    @Column(name = "status")
    private String status;

}
