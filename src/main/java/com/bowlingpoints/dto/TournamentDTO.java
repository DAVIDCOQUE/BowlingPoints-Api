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
    private List<Integer> categoryIds;
    private List<Integer> modalityIds;
    private List<String> categoryNames;
    private List<String> modalityNames;
    private List<CategoriesDTO> categories;
    private List<ModalitiesDTO> modalities;
}
