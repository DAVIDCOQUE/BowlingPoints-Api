package com.bowlingpoints.service;

import com.bowlingpoints.dto.TopTournamentDTO;
import com.bowlingpoints.dto.UserStatisticsDTO;
import com.bowlingpoints.dto.UserStatsProjection;
import com.bowlingpoints.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ResultRepository resultRepository;

    // Devuelve resumen de estadísticas generales
    public UserStatisticsDTO calculateUserStats(Integer userId) {
        UserStatsProjection projection = resultRepository.findStatsByUserId(userId);

        // Maneja nulos para evitar errores si el usuario no tiene resultados aún
        return UserStatisticsDTO.builder()
                .totalTournaments(projection != null ? projection.getTotalTournaments() : 0)
                .totalStrikes(projection != null ? projection.getTotalStrikes() : 0)
                .avgScore(projection != null ? projection.getAvgScore() : 0.0)
                .bestGame(projection != null ? projection.getBestGame() : 0)
                .tournamentsWon(projection != null ? projection.getTournamentsWon() : 0)
                .build();
    }

    // Devuelve los mejores torneos jugados por el usuario
    public List<TopTournamentDTO> getTopTournaments(Integer userId) {
        return resultRepository.findTopTournamentsByUser(userId);
    }
}
