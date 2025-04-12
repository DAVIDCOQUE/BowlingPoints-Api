package com.bowlingpoints.util;

import java.util.ArrayList;
import java.util.List;

public class Jugador {
    private String nombre;
    private String club;
    private List<Integer> puntajes;

    public Jugador(String nombre, String club) {
        this.nombre = nombre;
        this.club = club;
        this.puntajes = new ArrayList<>();
    }

    public void agregarPuntaje(int puntaje) {
        this.puntajes.add(puntaje);
    }

    // Getters y setters

    @Override
    public String toString() {
        return "Jugador{" +
                "nombre='" + nombre + '\'' +
                ", club='" + club + '\'' +
                ", puntajes=" + puntajes +
                '}';
    }
}
