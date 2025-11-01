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
 * Test unitario para UserTournamentService.
 */
class UserTournamentServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private TournamentRepository tournamentRepository; // no se usa directamente, pero lo requiere el constructor

    @InjectMocks
    private UserTournamentService userTournamentService;

    private Object[] rowData;
    private Result result;
    private UserStatisticsDTO mockStats;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Datos simulados para getTournamentsPlayedByUser()
        rowData = new Object[]{
                1,                    // tournamentId
                "Open Nacional",      // name
                LocalDate.of(2025, 3, 10), // date
                "Cali",               // location
                "Individual",         // modalidad
                "Elite",              // categoria
                6,                    // resultados
                "imagen.jpg"          // imageUrl
        };

        // Datos simulados para getResultsForUserAndTournament()
        result = Result.builder()
                .resultId(100)
                .score(220)
                .laneNumber(5)
                .lineNumber(2)
                .createdAt(LocalDateTime.of(2025, 3, 10, 18, 0))
                .build();

        // Datos simulados para getUserStatistics()
        mockStats = UserStatisticsDTO.builder()
                .totalTournaments(5)
                .totalStrikes(80)
                .avgScore(210.5)
                .bestGame(279)
                .tournamentsWon(2)
                .build();
    }

    // ----------------------------------------------------------------------
    // getTournamentsPlayedByUser
    // ----------------------------------------------------------------------
   /* @Test
    void getTournamentsPlayedByUser_ShouldReturnMappedDTOs_WhenDataExists() {
        when(resultRepository.findTournamentsByPersonId(1))
                .thenReturn(List.of(rowData));

        List<UserTournamentDTO> result = userTournamentService.getTournamentsPlayedByUser(1);

        assertEquals(1, result.size());
        UserTournamentDTO dto = result.get(0);
        assertEquals("Open Nacional", dto.getName());
        assertEquals("Cali", dto.getLocation());
        assertEquals(6, dto.getResultados());
        assertEquals("imagen.jpg", dto.getImageUrl());
        verify(resultRepository, times(1)).findTournamentsByPersonId(1);
    }*/

    @Test
    void getTournamentsPlayedByUser_ShouldReturnEmptyList_WhenNoData() {
        when(resultRepository.findTournamentsByPersonId(1))
                .thenReturn(List.of());

        List<UserTournamentDTO> result = userTournamentService.getTournamentsPlayedByUser(1);

        assertTrue(result.isEmpty());
        verify(resultRepository, times(1)).findTournamentsByPersonId(1);
    }

    // ----------------------------------------------------------------------
    // getResultsForUserAndTournament
    // ----------------------------------------------------------------------
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
    }

    // ----------------------------------------------------------------------
    // getUserStatistics
    // ----------------------------------------------------------------------
    /*@Test
    void getUserStatistics_ShouldReturnMappedStatistics() {
        when(resultRepository.findStatsByUserId(1))
                .thenReturn((UserStatsProjection) mockStats);

        UserStatisticsDTO stats = userTournamentService.getUserStatistics(1);

        assertNotNull(stats);
        assertEquals(5, stats.getTotalTournaments());
        assertEquals(80, stats.getTotalStrikes());
        assertEquals(210.5, stats.getAvgScore());
        assertEquals(279, stats.getBestGame());
        assertEquals(2, stats.getTournamentsWon());
        verify(resultRepository).findStatsByUserId(1);
    }

    /*@Test
    void getUserStatistics_ShouldHandleNullFieldsGracefully() {
        UserStatisticsDTO partial = UserStatisticsDTO.builder().build();
        when(resultRepository.findStatsByUserId(2)).thenReturn((UserStatsProjection) partial);

        UserStatisticsDTO stats = userTournamentService.getUserStatistics(2);

        assertNotNull(stats);
        assertNull(stats.getAvgScore());
        assertNull(stats.getTotalTournaments());
        verify(resultRepository).findStatsByUserId(2);
    }*/
}
