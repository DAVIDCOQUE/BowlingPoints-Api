package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerRankingDTO {
    private Integer personId;
    private String fullName;
    private Double averageScore;
    private String photoUrl;
}
