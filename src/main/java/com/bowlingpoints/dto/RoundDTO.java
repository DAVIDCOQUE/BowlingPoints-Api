package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundDTO {
    private Integer roundId;
    private Integer tournamentId;
    private Integer roundNumber;
}
