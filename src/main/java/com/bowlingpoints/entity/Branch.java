package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "rama")
public class Branch {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rama")
    private int idBranch;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "activo")
    private boolean active;

    @Column(name = "fecha_creacion")
    private Date created_at;

    @Column(name = "usuario_creacion")
    private String created_by;

    @Column(name = "fecha_actualizacion")
    private Date updated_at;

    @Column(name = "usuario_actualizacion")
    private String updated_by;


}
