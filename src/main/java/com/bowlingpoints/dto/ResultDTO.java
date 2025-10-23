package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultDTO {

    private Integer resultId;

    // Persona
    private Integer personId;
    private String personName;

    // Equipo
    private Integer teamId;
    private String teamName;

    // Torneo
    private Integer tournamentId;
    private String tournamentName;

    // Ronda
    private Integer roundNumber;

    // Categoría
    private Integer categoryId;
    private String categoryName;

    // Modalidad
    private Integer modalityId;
    private String modalityName;

    // Branch (Masculino, Femenino, Mixto)
    private String rama;

    // Datos del resultado
    private Integer laneNumber;
    private Integer lineNumber;
    private Integer score;
}
