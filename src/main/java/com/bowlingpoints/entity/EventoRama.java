package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "evento_rama")
public class EventoRama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_rama")
    private int idEventoRama;

    @Column(name = "id_evento", insertable=false, updatable=false)
    private int idEvento;

    @ManyToOne
    @JoinColumn(name = "id_evento", referencedColumnName = "id_evento")
    private Evento event;

    @ManyToOne
    @JoinColumn(name = "id_rama", referencedColumnName = "id_rama")
    private Rama branch;


}
