package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardPlayerDTO {
    private Integer personId;
    private String fullName;
    private Double averageScore;
    private Integer bestGame;
    private Long titlesWon;
    private String photoUrl;
}
