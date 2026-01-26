package com.bowlingpoints.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PlayerResultSummaryDTO {
    private Integer position;
    private Integer playerId;
    private String playerName;
    private List<PlayerModalitySummaryDTO> modalities;
    private Integer totalGlobal;
    private Double promedioGlobal;
    private Integer lineasGlobal;
}
