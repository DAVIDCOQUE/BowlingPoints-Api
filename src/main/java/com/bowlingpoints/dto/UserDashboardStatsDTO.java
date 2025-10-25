package com.bowlingpoints.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserDashboardStatsDTO {
    private Double avgScoreGeneral;
    private Integer bestLine;
    private Integer totalTournaments;
    private Integer totalLines;
    private TournamentAvgDTO bestTournamentAvg;
    private List<TournamentAvgDTO> avgPerTournament;
    private List<ModalityAvgDTO> avgPerModality;
    private List<ScoreRangeDTO> scoreDistribution;

    @Data
    @Builder
    public static class TournamentAvgDTO {
        private Integer tournamentId;
        private String tournamentName;
        private String imageUrl;
        private Double average;
        private LocalDate startDate;
    }

    @Data
    @Builder
    public static class ModalityAvgDTO {
        private String modalityName;
        private Double average;
    }

    @Data
    @Builder
    public static class ScoreRangeDTO {
        private String label;
        private Long count;
    }
}
