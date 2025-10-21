package com.bowlingpoints.dto;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentRegistrationDTO {
    private Integer registrationId;
    private Integer tournamentId;
    private String tournamentName;
    private Integer personId;
    private String personFullName;
    private Integer categoryId;
    private String categoryName;
    private Integer modalityId;
    private String modalityName;
    private Integer branchId;
    private String branchName;
    private Integer teamId;
    private String teamName;
    private Boolean status;
    private Date registrationDate;
}
