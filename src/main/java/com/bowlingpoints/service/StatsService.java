package com.bowlingpoints.service;

import com.bowlingpoints.dto.UserDashboardStatsDTO;
import com.bowlingpoints.entity.Result;
import com.bowlingpoints.repository.ResultRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ResultRepository resultRepository;

    @Transactional
    public UserDashboardStatsDTO getUserDashboardStats(Integer userId) {
        List<Result> results = resultRepository.findByPersonId(userId);

        if (results.isEmpty()) {
            return UserDashboardStatsDTO.builder()
                    .avgScoreGeneral(0.0)
                    .bestLine(0)
                    .totalLines(0)
                    .totalTournaments(0)
                    .avgPerTournament(List.of())
                    .avgPerModality(List.of())
                    .scoreDistribution(List.of())
                    .build();
        }

        // 1. Promedio general
        double avgScoreGeneral = results.stream()
                .mapToInt(Result::getScore)
                .average()
                .orElse(0.0);

        // 2. Mejor línea
        int bestLine = results.stream()
                .mapToInt(Result::getScore)
                .max()
                .orElse(0);

        // 3. Total líneas jugadas
        int totalLines = results.size();

        // 4. Total torneos
        long totalTournaments = results.stream()
                .map(r -> r.getTournament().getTournamentId())
                .distinct()
                .count();

        // 5. Promedio por torneo
        Map<Integer, List<Result>> groupedByTournament = results.stream()
                .collect(Collectors.groupingBy(r -> r.getTournament().getTournamentId()));

        List<UserDashboardStatsDTO.TournamentAvgDTO> avgPerTournament = groupedByTournament.entrySet().stream()
                .map(entry -> {
                    Integer tournamentId = entry.getKey();
                    List<Result> resList = entry.getValue();

                    double avg = resList.stream()
                            .mapToInt(Result::getScore)
                            .average()
                            .orElse(0.0);

                    var first = resList.get(0).getTournament();

                    return UserDashboardStatsDTO.TournamentAvgDTO.builder()
                            .tournamentId(tournamentId)
                            .tournamentName(first.getName())
                            .imageUrl(first.getImageUrl())
                            .average(avg)
                            .startDate(first.getStartDate())
                            .build();
                })
                .sorted(Comparator.comparing(UserDashboardStatsDTO.TournamentAvgDTO::getStartDate).reversed())
                .toList();

        // 6. Mejor torneo por promedio
        UserDashboardStatsDTO.TournamentAvgDTO bestTournamentAvg = avgPerTournament.stream()
                .max(Comparator.comparing(UserDashboardStatsDTO.TournamentAvgDTO::getAverage))
                .orElse(null);

        // 7. Promedio por modalidad
        Map<String, List<Result>> groupedByModality = results.stream()
                .filter(r -> r.getModality() != null)
                .collect(Collectors.groupingBy(r -> r.getModality().getName()));

        List<UserDashboardStatsDTO.ModalityAvgDTO> avgPerModality = groupedByModality.entrySet().stream()
                .map(entry -> {
                    String modalityName = entry.getKey();
                    List<Result> resList = entry.getValue();

                    double avg = resList.stream()
                            .mapToInt(Result::getScore)
                            .average()
                            .orElse(0.0);

                    return UserDashboardStatsDTO.ModalityAvgDTO.builder()
                            .modalityName(modalityName)
                            .average(avg)
                            .build();
                })
                .toList();

        // 8. Distribución de puntajes
        List<UserDashboardStatsDTO.ScoreRangeDTO> scoreDistribution = buildScoreRanges(results);

        return UserDashboardStatsDTO.builder()
                .avgScoreGeneral(avgScoreGeneral)
                .bestLine(bestLine)
                .totalLines(totalLines)
                .totalTournaments((int) totalTournaments)
                .avgPerTournament(avgPerTournament)
                .bestTournamentAvg(bestTournamentAvg)
                .avgPerModality(avgPerModality)
                .scoreDistribution(scoreDistribution)
                .build();
    }

    private List<UserDashboardStatsDTO.ScoreRangeDTO> buildScoreRanges(List<Result> results) {
        int[] ranges = {130, 160, 190, 220, 250, 300}; // Define los cortes de las categorías
        String[] labels = {"0–129", "130–160", "161–190", "191–220", "221–250", "251–300"};

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (String label : labels) {
            distribution.put(label, 0L);
        }

        for (Result result : results) {
            int score = result.getScore();
            String label = "";

            if (score < 130) label = "0–129";
            else if (score <= 160) label = "130–160";
            else if (score <= 190) label = "161–190";
            else if (score <= 220) label = "191–220";
            else if (score <= 250) label = "221–250";
            else label = "251–300";

            distribution.put(label, distribution.get(label) + 1);
        }

        return distribution.entrySet().stream()
                .map(e -> UserDashboardStatsDTO.ScoreRangeDTO.builder()
                        .label(e.getKey())
                        .count(e.getValue())
                        .build())
                .toList();
    }
}
