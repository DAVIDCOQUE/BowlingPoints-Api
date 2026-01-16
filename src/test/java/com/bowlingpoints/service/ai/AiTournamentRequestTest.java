package com.bowlingpoints.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiTournamentRequestTest {

    @Test
    void shouldCreateRequestWithAllFields() {
        AiTournamentRequest request = new AiTournamentRequest(1, 2, 3);

        assertEquals(1, request.tournamentId());
        assertEquals(2, request.categoryId());
        assertEquals(3, request.branchId());
    }

    @Test
    void shouldAllowNullFields() {
        AiTournamentRequest request = new AiTournamentRequest(null, null, null);

        assertNull(request.tournamentId());
        assertNull(request.categoryId());
        assertNull(request.branchId());
    }

    @Test
    void shouldAllowPartialNullFields() {
        AiTournamentRequest request = new AiTournamentRequest(1, null, 3);

        assertEquals(1, request.tournamentId());
        assertNull(request.categoryId());
        assertEquals(3, request.branchId());
    }
}
