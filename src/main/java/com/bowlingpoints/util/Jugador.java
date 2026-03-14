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

    public String getNombre() { return nombre; }
    public String getClub() { return club; }
    public List<Integer> getPuntajes() { return puntajes; }

    @Override
    public String toString() {
        return "Jugador{" +
                "nombre='" + nombre + '\'' +
                ", club='" + club + '\'' +
                ", puntajes=" + puntajes +
                '}';
    }
}
