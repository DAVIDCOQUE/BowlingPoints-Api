package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "Categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private int idCategoria;

    @Column(name = "nombre_categoria")
    private String nombreCategoria;

    @Column(name = "marca_categoria")
    private int marcaCategoria;

    @Column(name = "edad_minima_categoria")
    private int edadMinima;

    @Column(name = "edad_maxima_categoria")
    private int edadMaxima;

    @Column(name = "activo")
    private boolean activo;

    @Column(name = "fecha_creacion")
    private Date created_at;

    @Column(name = "usuario_creacion")
    private String created_by;

    @Column(name = "fecha_actualizacion")
    private Date updated_at;

    @Column(name = "usuario_actualizacion")
    private String updated_by;
}
