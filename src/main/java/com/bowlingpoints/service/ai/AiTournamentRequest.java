package com.bowlingpoints.service.ai;

public record AiTournamentRequest(
        Integer tournamentId,
        Integer categoryId,
        Integer branchId
) {}