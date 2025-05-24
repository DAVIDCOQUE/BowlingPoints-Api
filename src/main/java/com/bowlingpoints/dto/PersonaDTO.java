package com.bowlingpoints.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PersonaDTO {

    Integer id;
    String firstName;
    String secondName;
    String lastName;
    String secondLastName;
    String gender;
    String email;
    String mobile;
}
