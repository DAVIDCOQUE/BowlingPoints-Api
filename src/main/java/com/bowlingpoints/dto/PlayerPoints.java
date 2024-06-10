package com.bowlingpoints.dto;

import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.entity.Persona;
import lombok.Data;

@Data
public class PlayerPoints {

    private Persona person;
    private DetailsRound rounds;

}
