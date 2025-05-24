package com.bowlingpoints.service;


import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TournamentService {

    @Autowired
    TournamentRepository tournamentRepository;

    public List<TournamentDTO> getAllTournaments(){

        List<TournamentDTO> tournamentDTOList = new ArrayList<>();

        List<Tournament> tournamentList = tournamentRepository.findAll();

        tournamentList.forEach(tournament -> {
            tournamentDTOList.add(TournamentDTO.builder()
                            .tournamentName(tournament.getTournamentName())
                            .category("Sub-21")
                            .endDate(String.valueOf(tournament.getEndDate()))
                            .startDate(String.valueOf(tournament.getStartDate()))
                            .modality("Individual")
                            .place("Cali, Valle")
                    .build());
        });


        return tournamentDTOList;
    }
}
