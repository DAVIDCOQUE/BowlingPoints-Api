package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTournamentService {
    private final ResultRepository resultRepository;
    private final TournamentRepository tournamentRepository;

    // Lista los torneos jugados por el usuario (solo los que tienen resultados)
    public List<UserTournamentDTO> getTournamentsPlayedByUser(Integer userId) {
        return resultRepository.findTournamentsByPersonId(userId).stream()
                .map(obj -> UserTournamentDTO.builder()
                        .tournamentId((Integer) obj[0])
                        .name((String) obj[1])
                        .date((java.time.LocalDate) obj[2])
                        .location((String) obj[3])
                        .modalidad((String) obj[4])
                        .categoria((String) obj[5])
                        .resultados(((Number) obj[6]).intValue())
                        .imageUrl((String) obj[7])
                        .build())
                .collect(Collectors.toList());
    }

    // Detalle de los resultados de un usuario en un torneo
    public List<UserTournamentResultDTO> getResultsForUserAndTournament(Integer userId, Integer tournamentId) {
        return resultRepository.findResultsByPersonAndTournament(userId, tournamentId).stream()
                .map(r -> UserTournamentResultDTO.builder()
                        .resultId(r.getResultId())
                        .score(r.getScore())
                        .laneNumber(r.getLaneNumber())
                        .lineNumber(r.getLineNumber())
                        .playedAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Estadísticas generales para "Mis Resultados"
    public UserStatisticsDTO getUserStatistics(Integer userId) {
        var stats = resultRepository.findStatsByUserId(userId);

        if (stats == null) {
            return new UserStatisticsDTO(); // ✅ evita NullPointerException
        }

        return UserStatisticsDTO.builder()
                .totalTournaments(stats.getTotalTournaments())
                .totalStrikes(stats.getTotalStrikes())
                .avgScore(stats.getAvgScore())
                .bestGame(stats.getBestGame())
                .tournamentsWon(stats.getTournamentsWon())
                .build();
    }
}
