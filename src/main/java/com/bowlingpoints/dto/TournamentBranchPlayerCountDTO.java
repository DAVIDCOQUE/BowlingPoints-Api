package com.bowlingpoints.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentBranchPlayerCountDTO {
    private Integer branchId;
    private String branchName;
    private Long playerCount;
}
