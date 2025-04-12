package com.bowlingpoints.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoCategorias {

    private String nombreCategoria;
    private int numeroJugadores;
}
