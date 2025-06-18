package com.bowlingpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubsDTO {

    private Integer clubId;
    private String name;
    private LocalDate foundationDate;
    private String city;
    private String description;
    private String imageUrl;
    private Boolean status;

    // 👇 Agregado: miembros del club (para creación/actualización)
    private List<ClubMemberRequestDTO> members;
}
