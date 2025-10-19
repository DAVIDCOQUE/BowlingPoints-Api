package com.bowlingpoints.dto;

import com.bowlingpoints.dto.response.CategoriesDTO;
import com.bowlingpoints.dto.response.ModalitiesDTO;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentDTO {

    private Integer tournamentId;
    private String name;
    private String organizer;
    private Integer ambitId;
    private String ambitName;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String stage;
    private Boolean status;

    // Estos dos campos son necesarios porque el front los está enviando así
    private List<Integer> categoryIds;
    private List<Integer> modalityIds;

    //  Estos son para mostrar los nombres ya asignados desde backend
    private List<String> categoryNames;
    private List<String> modalityNames;

    // Estos se llenan en toDTO() para mostrar los objetos completos
    private List<CategoriesDTO> categories;
    private List<ModalitiesDTO> modalities;
}