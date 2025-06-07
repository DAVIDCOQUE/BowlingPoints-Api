package com.bowlingpoints.controller;


import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("updateTournament/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> updateTournament(
            @PathVariable Integer id,
            @RequestBody TournamentDTO dto
    ) {
        try {
            boolean updated = tournamentService.updateTournament(id, dto);

            if (!updated) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ResponseGenericDTO<>(false, "Torneo no encontrado", null));
            }

            return ResponseEntity
                    .ok(new ResponseGenericDTO<>(true, "Torneo actualizado correctamente", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseGenericDTO<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseGenericDTO<>(false, "Error interno al actualizar el torneo", null));
        }
    }

    @DeleteMapping("deleteTournament/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> deleteTournament(@PathVariable Integer id) {
        try {
            boolean deleted = tournamentService.deleteTournament(id);

            if (!deleted) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ResponseGenericDTO<>(false, "Torneo no encontrado", null));
            }

            return ResponseEntity
                    .ok(new ResponseGenericDTO<>(true, "Torneo eliminado correctamente", null));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseGenericDTO<>(false, "Error al eliminar el torneo", null));
        }
    }

    @PostMapping("createTournament")
    public ResponseEntity<ResponseGenericDTO<TournamentDTO>> createTournament(@RequestBody TournamentDTO dto) {
        try {
            // Simulamos que el usuario creador es 1
            Tournament saved = tournamentService.saveTournament(dto, 1);

            TournamentDTO responseDto = TournamentDTO.builder().build();
            responseDto.setTournamentName(saved.getTournamentName());
            responseDto.setStartDate(saved.getStartDate().toString());
            responseDto.setEndDate(saved.getEndDate().toString());
            responseDto.setPlace(saved.getPlace());
            responseDto.setCauseStatus(saved.getCauseStatus());

            return ResponseEntity
                    .status(201)
                    .body(new ResponseGenericDTO<>(true, "Torneo creado correctamente", responseDto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseGenericDTO<>(false, e.getMessage(), null));
        }
    }


}
