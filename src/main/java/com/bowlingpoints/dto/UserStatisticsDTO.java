package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserStatisticsDTO {
    private Integer totalTournaments;
    private Integer totalStrikes;         // Chuzas totales
    private Double avgScore;              // Promedio por partida
    private Integer bestGame;             // Mejor partida
    private Integer tournamentsWon;       // Torneos ganados
    // Puedes agregar más según quieras mostrar gráficas (histograma, etc.)

    private Integer personId;
    private String fullName;
    private String club;
    private String age;
    private String photoUrl;
}