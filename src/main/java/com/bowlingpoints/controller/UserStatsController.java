package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
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

    @GetMapping("/dashboard")
    public ResponseEntity<ResponseGenericDTO<UserDashboardStatsDTO>> getDashboardStats(
            @RequestParam Integer userId) {
        var data = statsService.getUserDashboardStats(userId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Estadísticas para dashboard cargadas", data));
    }

    // Público (uso en vista de jugador)
    @GetMapping("/public-summary")
    public ResponseEntity<ResponseGenericDTO<UserStatisticsDTO>> getPublicPlayerSummary(
            @RequestParam Integer userId) {
        var data = statsService.getUserPublicStats(userId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resumen público del jugador cargado", data));
    }
}
