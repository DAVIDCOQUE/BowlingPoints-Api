package com.bowlingpoints.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubDTO {
    private Integer clubId;
    private String name;
    private LocalDate foundationDate;
    private String city;
    private String description;
    private String imageUrl;
    private Boolean status;
    private List<ClubPersonDTO> members; // Lista de miembros del club
}
