package com.bowlingpoints.dto.files;

public record TournamentRegistrationRow(
        String documentNumber,
        String tournamentName,
        String categoryName,
        String modalityName,
        String branchName,
        String teamName,
        int lineNumber
) {
}
