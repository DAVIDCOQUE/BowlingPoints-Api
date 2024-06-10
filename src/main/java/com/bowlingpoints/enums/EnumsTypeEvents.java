package com.bowlingpoints.enums;

public enum EnumsTypeEvents {

    Nacional(1),
    Departamental(2),
    Internacional(3);


    private final Integer identificador;

    // Constructor
    EnumsTypeEvents(Integer identificador) {
        this.identificador = identificador;
    }

    // Método para obtener la descripción
    public Integer getIdentificador() {
        return identificador;
    }

}
