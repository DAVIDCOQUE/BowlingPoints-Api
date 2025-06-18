package com.bowlingpoints.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateClubRequestDTO {

    private String name;
    private String city;
    private String description;
    private LocalDate foundationDate;
    private Boolean status;
    private List<Integer> members;
}
