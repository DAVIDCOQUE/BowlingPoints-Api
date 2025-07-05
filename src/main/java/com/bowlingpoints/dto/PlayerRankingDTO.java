package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerRankingDTO {
    private Integer personId;
    private String fullName;
    private Double averageScore;
    private Integer bestGame;
    private Integer titlesWon;
    private String photoUrl;

    public PlayerRankingDTO(Integer personId, String fullName, Double avgScore, String photoUrl) {
        this.personId = personId;
        this.fullName = fullName;
        this.averageScore = avgScore;
        this.photoUrl = photoUrl;
    }
}
