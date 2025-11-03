package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.Result;
import com.bowlingpoints.repository.ResultRepository;
import com.bowlingpoints.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario completo para UserTournamentService.
 */
class UserTournamentServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @InjectMocks
    private UserTournamentService userTournamentService;

    private Object[] rowData;
    private Result result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        rowData = new Object[]{
                1, "Open Nacional", LocalDate.of(2025, 3, 10),
                "Cali", "Individual", "Elite", 6, "imagen.jpg"
        };

        result = Result.builder()
                .resultId(100)
                .score(220)
                .laneNumber(5)
                .lineNumber(2)
                .createdAt(LocalDateTime.of(2025, 3, 10, 18, 0))
                .build();
    }

    @Test
    void getTournamentsPlayedByUser_ShouldReturnMappedDTOs_WhenDataExists() {
        when(resultRepository.findTournamentsByPersonId(1))
                .thenReturn(List.<Object[]>of(rowData)); // ✅ tipo correcto

        List<UserTournamentDTO> result = userTournamentService.getTournamentsPlayedByUser(1);

        assertEquals(1, result.size());
        UserTournamentDTO dto = result.get(0);
        assertEquals("Open Nacional", dto.getName());
        assertEquals("Cali", dto.getLocation());
        assertEquals(6, dto.getResultados());
        assertEquals("imagen.jpg", dto.getImageUrl());
        verify(resultRepository).findTournamentsByPersonId(1);
    }

    @Test
    void getTournamentsPlayedByUser_ShouldReturnEmptyList_WhenNoData() {
        when(resultRepository.findTournamentsByPersonId(1)).thenReturn(List.of());

        List<UserTournamentDTO> result = userTournamentService.getTournamentsPlayedByUser(1);

        assertTrue(result.isEmpty());
        verify(resultRepository).findTournamentsByPersonId(1);
    }

    @Test
    void getResultsForUserAndTournament_ShouldReturnMappedResults() {
        when(resultRepository.findResultsByPersonAndTournament(1, 10))
                .thenReturn(List.of(result));

        List<UserTournamentResultDTO> results =
                userTournamentService.getResultsForUserAndTournament(1, 10);

        assertEquals(1, results.size());
        UserTournamentResultDTO dto = results.get(0);
        assertEquals(220, dto.getScore());
        assertEquals(5, dto.getLaneNumber());
        assertEquals(2, dto.getLineNumber());
        assertEquals(result.getCreatedAt(), dto.getPlayedAt());
        verify(resultRepository).findResultsByPersonAndTournament(1, 10);
    }

    @Test
    void getResultsForUserAndTournament_ShouldReturnEmptyList_WhenNoResults() {
        when(resultRepository.findResultsByPersonAndTournament(1, 10))
                .thenReturn(List.of());

        List<UserTournamentResultDTO> results =
                userTournamentService.getResultsForUserAndTournament(1, 10);

        assertTrue(results.isEmpty());
        verify(resultRepository).findResultsByPersonAndTournament(1, 10);
    }

    @Test
    void getUserStatistics_ShouldReturnMappedStatistics() {
        UserStatsProjection stats = mock(UserStatsProjection.class); // ✅ import correcto

        when(stats.getTotalTournaments()).thenReturn(5);
        when(stats.getTotalStrikes()).thenReturn(80);
        when(stats.getAvgScore()).thenReturn(210.5);
        when(stats.getBestGame()).thenReturn(279);
        when(stats.getTournamentsWon()).thenReturn(2);

        when(resultRepository.findStatsByUserId(1)).thenReturn(stats);

        UserStatisticsDTO result = userTournamentService.getUserStatistics(1);

        assertNotNull(result);
        assertEquals(5, result.getTotalTournaments());
        assertEquals(80, result.getTotalStrikes());
        assertEquals(210.5, result.getAvgScore());
        assertEquals(279, result.getBestGame());
        assertEquals(2, result.getTournamentsWon());
        verify(resultRepository).findStatsByUserId(1);
    }

    @Test
    void getUserStatistics_ShouldHandleNullProjectionGracefully() {
        when(resultRepository.findStatsByUserId(2)).thenReturn(null);

        UserStatisticsDTO result = userTournamentService.getUserStatistics(2);

        assertNotNull(result);
        assertNull(result.getTotalTournaments());
        assertNull(result.getAvgScore());
        verify(resultRepository).findStatsByUserId(2);
    }
}
