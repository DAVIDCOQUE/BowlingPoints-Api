package com.bowlingpoints.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDTO {
    private Integer teamId;
    private String nameTeam;
    private String phone;
    private Boolean status;

    private List<Integer> playerIds;

    private Integer categoryId;
    private Integer modalityId;
    private Integer tournamentId;
}