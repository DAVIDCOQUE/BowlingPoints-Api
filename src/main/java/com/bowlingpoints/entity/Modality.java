package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "modalidad")
public class Modality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modalidad")
    private int idModalidad;


    @Column(name = "descripcion")
    private String description;

    @Column(name = "activo")
    private String active;

    @Column(name = "fecha_creacion")
    private String created_at;

    @Column(name = "usuario_creacion")
    private String created_by;

    @Column(name = "fecha_actualizacion")
    private String updated_at;

    @Column(name = "usuario_actualizacion")
    private String updated_by;
}
