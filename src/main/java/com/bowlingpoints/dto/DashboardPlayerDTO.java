package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardPlayerDTO {
    private Integer position;
    private Integer personId;
    private String fullName;
    private Double averageScore;
    private Integer bestGame;
    private Long titlesWon;
    private String photoUrl;

    // Constructor para JPQL (sin position, se asigna en el servicio)
    public DashboardPlayerDTO(Integer personId, String fullName, Double averageScore,
                              Integer bestGame, Long titlesWon, String photoUrl) {
        this.personId = personId;
        this.fullName = fullName;
        this.averageScore = averageScore;
        this.bestGame = bestGame;
        this.titlesWon = titlesWon;
        this.photoUrl = photoUrl;
    }
}
