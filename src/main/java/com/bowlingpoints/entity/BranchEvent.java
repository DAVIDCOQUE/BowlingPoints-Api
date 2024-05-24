package com.bowlingpoints.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "evento_rama")
public class BranchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_rama")
    private int idEventoRama;

    @ManyToOne
    @JoinColumn(name = "id_evento", referencedColumnName = "id_evento")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "id_rama", referencedColumnName = "id_rama")
    private Branch branch;


}
