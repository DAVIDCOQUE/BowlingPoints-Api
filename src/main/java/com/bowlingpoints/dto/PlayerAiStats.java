package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAiStats {

    private String playerName;
    private String modalityName;

    private double averageScore;
    private int gamesPlayed;

    private int maxScore;
    private int minScore;
}
