package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "evento_categoria")
public class EventoCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_categoria")
    private int idBranch;

    @Column(name = "id_evento", insertable = false, updatable = false)
    private int idEvento;

    @Column(name = "id_categoria", insertable = false, updatable = false)
    private int idCategoria;

    @ManyToOne
    @JoinColumn(name = "id_evento", referencedColumnName = "id_evento")
    private Evento evento;

    @ManyToOne
    @JoinColumn(name = "id_categoria", referencedColumnName = "id_categoria")
    private Categoria categoria;

}
