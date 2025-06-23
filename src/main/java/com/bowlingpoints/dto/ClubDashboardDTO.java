package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubDashboardDTO {
    private Integer clubId;
    private String name;
    private Integer totalScore;
}
