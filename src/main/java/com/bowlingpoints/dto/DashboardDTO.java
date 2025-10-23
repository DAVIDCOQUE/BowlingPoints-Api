package com.bowlingpoints.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DashboardDTO {
    private List<TournamentDTO> scheduledOrPostponedTournaments;
    private List<TournamentDTO> inProgressTournaments;
    private List<DashboardPlayerDTO> topPlayers;
    private List<AmbitDTO> ambits;
}