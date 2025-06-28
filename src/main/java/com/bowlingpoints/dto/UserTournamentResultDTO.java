package com.bowlingpoints.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTournamentResultDTO {
    private Integer resultId;
    private Integer score;
    private String ronda;              // Ronda/Etapa
    private Integer laneNumber;
    private Integer lineNumber;
    private LocalDateTime playedAt;    // Fecha/Hora partida
}
