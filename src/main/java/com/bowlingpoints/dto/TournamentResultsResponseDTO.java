package com.bowlingpoints.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentResultsResponseDTO {

    private TournamentSummary tournament;

    private List<ModalityDTO> modalities;
    private List<Integer> rounds;

    private List<PlayerResultTableDTO> results;
    private List<PlayerByModalityDTO> resultsByModality;


    private Map<String, Double> avgByLine;
    private Double avgByRound;
    private HighestLineDTO highestLine;


    @Data
    @Builder
    public static class TournamentSummary {
        private Integer tournamentId;
        private String tournamentName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String location;
        private String imageUrl;
        private Boolean status;
    }
}
