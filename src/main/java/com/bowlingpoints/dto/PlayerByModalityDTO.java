package com.bowlingpoints.dto;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class PlayerByModalityDTO {
    private Integer position;
    private Integer personId;
    private String playerName;
    private String clubName;

    @Builder.Default
    private Map<String, Integer> modalityScores = new HashMap<>();

    private Integer total;
    private Integer linesPlayed;
    private Double promedio;
}
