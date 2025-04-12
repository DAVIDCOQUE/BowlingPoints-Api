package com.bowlingpoints.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RondaDTO implements Comparable<RondaDTO> {

    private int puesto;
    private String nombres;
    private String apellidos;
    private String club;
    private List<Integer> juegos;
    private int total;
    private double promedio;

    @Override
    public int compareTo(RondaDTO other) {
        return Double.compare(other.promedio,this.promedio);
    }
}
