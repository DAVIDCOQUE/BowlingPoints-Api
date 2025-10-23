package com.bowlingpoints.dto;

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

    //  IDs usados por el frontend
    private List<Integer> categoryIds;
    private List<Integer> modalityIds;
    private List<Integer> branchIds;

    //  Nombres usados por el frontend
    private List<String> categoryNames;
    private List<String> modalityNames;
    private List<String> branchNames;

    //  Objetos completos desde backend
    private List<CategoryDTO> categories;
    private List<ModalityDTO> modalities;
    private List<BranchDTO> branches;

    private List<TournamentRegistrationDTO> tournamentRegistrations;
}
