package com.bowlingpoints.dto;

import com.bowlingpoints.entity.Ambit;
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
    private Integer ambitId;
    private String ambitName;
    private String location;
    private String causeStatus;
    private Boolean status;
}
