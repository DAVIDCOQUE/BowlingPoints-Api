package com.bowlingpoints.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TournamentDTO {

    String tournamentName;
    String startDate;
    String endDate;
    String place;
    String modality;
    String category;
    String causeStatus;
    String status;

}
