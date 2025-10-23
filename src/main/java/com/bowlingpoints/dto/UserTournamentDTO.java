package com.bowlingpoints.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserTournamentDTO {
    private Integer tournamentId;
    private String name;
    private LocalDate date;        // Fecha de inicio
    private String location;
    private String modalidad;      // Modalidad principal jugada
    private String categoria;      // Categoría principal jugada
    private Integer resultados;    // Número de resultados/partidas jugadas
    private String imageUrl;       // Imagen torneo
    private Integer posicionFinal; // (opcional)
}
