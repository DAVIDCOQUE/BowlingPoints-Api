package com.bowlingpoints.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private List<TournamentDTO> activeTournaments;
    private List<PlayerRankingDTO> topPlayers;
    private List<ClubDashboardDTO> topClubs;
    private List<AmbitDTO> ambits;
}
