package com.bowlingpoints.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventoCategorias {

    private String nombreCategoria;
    private int numeroJugadores;
}
