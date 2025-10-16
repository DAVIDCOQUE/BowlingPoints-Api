package com.bowlingpoints.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TopTournamentDTO {
    private Integer tournamentId;
    private String name;
    private String imageUrl;
    private LocalDate startDate;
    private Integer bestScore;

}
