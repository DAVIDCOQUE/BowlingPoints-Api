package com.bowlingpoints.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighestLineDTO {
    private Integer score;
    private String playerName;
    private Integer lineNumber;
}
