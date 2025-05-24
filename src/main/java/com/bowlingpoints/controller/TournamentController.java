package com.bowlingpoints.controller;


import com.bowlingpoints.dto.PersonaDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.service.PersonaService;
import com.bowlingpoints.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("tournament/v1")
public class TournamentController {

    @Autowired
    TournamentService tournamentService;

    @GetMapping(value = "/all")
    public ResponseGenericDTO<List<TournamentDTO>> getAllTournaments() {

        List<TournamentDTO> personaList = tournamentService.getAllTournaments();

        return new ResponseGenericDTO<>(true,"Lista de torneos entregada con exito",personaList);
    }

}
