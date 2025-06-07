package com.bowlingpoints.dto;

import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.entity.Person;
import lombok.Data;

@Data
public class PlayerPoints {

    private Person person;
    private DetailsRound rounds;

}
