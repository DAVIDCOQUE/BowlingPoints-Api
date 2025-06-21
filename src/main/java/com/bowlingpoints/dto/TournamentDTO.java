package com.bowlingpoints.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentDTO {
    private Integer tournamentId;
    private String name;
    private Integer modalityId;
    private String modalityName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String causeStatus;
    private Boolean status;
}
