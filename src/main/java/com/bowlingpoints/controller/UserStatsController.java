package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TopTournamentDTO;
import com.bowlingpoints.dto.UserStatisticsDTO;
import com.bowlingpoints.dto.UserTournamentResultDTO;
import com.bowlingpoints.service.StatsService;
import com.bowlingpoints.service.UserTournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-stats")
@RequiredArgsConstructor
public class UserStatsController {
    private final StatsService statsService;
    private final UserTournamentService userTournamentService;

    @GetMapping("/summary")
    public ResponseEntity<ResponseGenericDTO<UserStatisticsDTO>> getStats(@RequestParam Integer userId) {
        UserStatisticsDTO stats = statsService.calculateUserStats(userId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Estadísticas obtenidas correctamente", stats));
    }

    @GetMapping("/top-tournaments")
    public ResponseEntity<ResponseGenericDTO<List<TopTournamentDTO>>> getTopTournaments(@RequestParam Integer userId) {
        List<TopTournamentDTO> topTorneos = statsService.getTopTournaments(userId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Top torneos obtenidos correctamente", topTorneos));
    }

    // 2. Detalle de resultados de un torneo jugado
    @GetMapping("/{userId}/tournament/{tournamentId}/results")
    public ResponseEntity<ResponseGenericDTO<List<UserTournamentResultDTO>>> getResultsForTournament(
            @PathVariable Integer userId,
            @PathVariable Integer tournamentId) {
        List<UserTournamentResultDTO> data = userTournamentService.getResultsForUserAndTournament(userId, tournamentId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultados del torneo obtenidos correctamente", data));
    }
}
