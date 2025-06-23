package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.repository.AmbitRepository;
import com.bowlingpoints.repository.ClubsRepository;
import com.bowlingpoints.repository.ResultRepository;
import com.bowlingpoints.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TournamentRepository tournamentRepository;
    private final ResultRepository resultRepository;
    private final ClubsRepository clubRepository;
    private final AmbitRepository ambitRepository;

    public DashboardDTO getDashboardData() {
        List<TournamentDTO> activeTournaments = tournamentRepository.findActiveTournaments();

        List<PlayerRankingDTO> topPlayers = resultRepository.findTop10PlayersByAvgScore();

        // Aquí está el cambio importante:
        List<ClubDashboardDTO> topClubs = resultRepository.findTopClubsRaw().stream()
                .map(row -> ClubDashboardDTO.builder()
                        .clubId(((Number) row[0]).intValue())
                        .name((String) row[1])
                        .totalScore(row[2] != null ? ((Number) row[2]).intValue() : 0)
                        .build())
                .collect(Collectors.toList());

        List<AmbitDTO> ambits = ambitRepository.findDistinctWithTournaments();

        return DashboardDTO.builder()
                .activeTournaments(activeTournaments)
                .topPlayers(topPlayers)
                .topClubs(topClubs)
                .ambits(ambits)
                .build();
    }
}
