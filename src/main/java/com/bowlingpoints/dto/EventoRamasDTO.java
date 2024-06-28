package com.bowlingpoints.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EventoRamasDTO {

    private String nombreRama;
    private int cantidadJugadores;
}
