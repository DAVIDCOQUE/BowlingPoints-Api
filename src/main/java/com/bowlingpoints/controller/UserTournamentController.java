package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.service.UserTournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-tournaments")
@RequiredArgsConstructor
public class UserTournamentController {
    private final UserTournamentService userTournamentService;

    // 1. Listar torneos jugados por el usuario
    @GetMapping("/{userId}/played")
    public ResponseEntity<ResponseGenericDTO<List<UserTournamentDTO>>> getPlayedTournaments(@PathVariable Integer userId) {
        List<UserTournamentDTO> data = userTournamentService.getTournamentsPlayedByUser(userId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Torneos jugados obtenidos correctamente", data));
    }

}
