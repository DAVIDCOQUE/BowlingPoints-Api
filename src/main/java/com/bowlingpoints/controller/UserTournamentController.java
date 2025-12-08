package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.service.UserTournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-tournaments")
@RequiredArgsConstructor
public class UserTournamentController {
    private final UserTournamentService userTournamentService;


    @GetMapping("/player/{personId}/grouped")
    public ResponseEntity<ResponseGenericDTO<Map<String, List<TournamentDTO>>>> getTournamentsByPlayerGrouped(
            @PathVariable Integer personId) {

        Map<String, List<TournamentDTO>> result = userTournamentService.getTournamentsByPlayerGrouped(personId);

        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Torneos agrupados correctamente", result)
        );
    }
}
