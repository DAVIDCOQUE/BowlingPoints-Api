package com.bowlingpoints.enums;

public enum EnumsEstadoEvento {

    EnCurso(1),
    Finalizado(2),
    Pendiente(3);


    private final Integer identificador;

    // Constructor
    EnumsEstadoEvento(Integer identificador) {
        this.identificador = identificador;
    }

    // Método para obtener la descripción
    public Integer getIdentificador() {
        return identificador;
    }

}
