package com.bowlingpoints.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TournamentSummaryDTO {
    private Integer tournamentId;
    private String organizer;
    private String tournamentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private List<String> modalities;
    private List<String> categories;
    private Integer totalMasculino;
    private Integer totalFemenino;
}
