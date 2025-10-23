package com.bowlingpoints.service;

import com.bowlingpoints.dto.UserStatisticsDTO;
import com.bowlingpoints.dto.UserStatsProjection;
import com.bowlingpoints.dto.UserTournamentDTO;
import com.bowlingpoints.dto.UserTournamentResultDTO;
import com.bowlingpoints.entity.Result;
import com.bowlingpoints.repository.ResultRepository;
import com.bowlingpoints.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTournamentServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @InjectMocks
    private UserTournamentService userTournamentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTournamentsPlayedByUser_ShouldReturnUserTournaments() {
        // Arrange
        Integer userId = 1;
        Object[] tournament1 = new Object[]{
            1,                      // tournamentId
            "Tournament 1",         // name
            LocalDate.now(),        // date
            "Location 1",          // location
            "Singles",             // modalidad
            "A",                   // categoria
            5,                     // resultados (número de partidas)
            "/tournament1.jpg"     // imageUrl
        };

        Object[] tournament2 = new Object[]{
            2,                      // tournamentId
            "Tournament 2",         // name
            LocalDate.now().plusDays(7),  // date
            "Location 2",          // location
            "Doubles",             // modalidad
            "B",                   // categoria
            3,                     // resultados
            "/tournament2.jpg"     // imageUrl
        };

        when(resultRepository.findTournamentsByPersonId(userId))
                .thenReturn(Arrays.asList(tournament1, tournament2));

        // Act
        List<UserTournamentDTO> result = userTournamentService.getTournamentsPlayedByUser(userId);

        // Assert
        assertEquals(2, result.size());
        
        UserTournamentDTO firstTournament = result.get(0);
        assertEquals(1, firstTournament.getTournamentId());
        assertEquals("Tournament 1", firstTournament.getName());
        assertEquals("Singles", firstTournament.getModalidad());
        assertEquals("A", firstTournament.getCategoria());
        assertEquals(5, firstTournament.getResultados());
        assertEquals("/tournament1.jpg", firstTournament.getImageUrl());

        UserTournamentDTO secondTournament = result.get(1);
        assertEquals(2, secondTournament.getTournamentId());
        assertEquals("Tournament 2", secondTournament.getName());
        assertEquals("Doubles", secondTournament.getModalidad());
        assertEquals("B", secondTournament.getCategoria());
        assertEquals(3, secondTournament.getResultados());
        assertEquals("/tournament2.jpg", secondTournament.getImageUrl());

        verify(resultRepository).findTournamentsByPersonId(userId);
    }

    @Test
    void getTournamentsPlayedByUser_WhenNoTournaments_ShouldReturnEmptyList() {
        // Arrange
        Integer userId = 1;
        when(resultRepository.findTournamentsByPersonId(userId))
                .thenReturn(Arrays.asList());

        // Act
        List<UserTournamentDTO> result = userTournamentService.getTournamentsPlayedByUser(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(resultRepository).findTournamentsByPersonId(userId);
    }

    @Test
    void getResultsForUserAndTournament_ShouldReturnUserResults() {
        // Arrange
        Integer userId = 1;
        Integer tournamentId = 1;

        Round round1 = Round.builder()
                .roundId(1)
                .roundNumber(1)
                .build();

        Round round2 = Round.builder()
                .roundId(2)
                .roundNumber(2)
                .build();

        Result result1 = Result.builder()
                .resultId(1)
                .score(180)
                .round(round1)
                .laneNumber(1)
                .lineNumber(1)
                .createdAt(LocalDateTime.now())
                .build();

        Result result2 = Result.builder()
                .resultId(2)
                .score(200)
                .round(round2)
                .laneNumber(2)
                .lineNumber(2)
                .createdAt(LocalDateTime.now().plusHours(1))
                .build();

        when(resultRepository.findResultsByPersonAndTournament(userId, tournamentId))
                .thenReturn(Arrays.asList(result1, result2));

        // Act
        List<UserTournamentResultDTO> results = userTournamentService
                .getResultsForUserAndTournament(userId, tournamentId);

        // Assert
        assertEquals(2, results.size());

        UserTournamentResultDTO firstResult = results.get(0);
        assertEquals(1, firstResult.getResultId());
        assertEquals(180, firstResult.getScore());
        assertEquals("Ronda 1", firstResult.getRonda());
        assertEquals(1, firstResult.getLaneNumber());
        assertEquals(1, firstResult.getLineNumber());

        UserTournamentResultDTO secondResult = results.get(1);
        assertEquals(2, secondResult.getResultId());
        assertEquals(200, secondResult.getScore());
        assertEquals("Ronda 2", secondResult.getRonda());
        assertEquals(2, secondResult.getLaneNumber());
        assertEquals(2, secondResult.getLineNumber());

        verify(resultRepository).findResultsByPersonAndTournament(userId, tournamentId);
    }

    @Test
    void getResultsForUserAndTournament_WhenNoResults_ShouldReturnEmptyList() {
        // Arrange
        Integer userId = 1;
        Integer tournamentId = 1;
        when(resultRepository.findResultsByPersonAndTournament(userId, tournamentId))
                .thenReturn(List.of());

        // Act
        List<UserTournamentResultDTO> results = userTournamentService
                .getResultsForUserAndTournament(userId, tournamentId);

        // Assert
        assertTrue(results.isEmpty());
        verify(resultRepository).findResultsByPersonAndTournament(userId, tournamentId);
    }

    @Test
    void getUserStatistics_ShouldReturnUserStats() {
        // Arrange
        Integer userId = 1;
        UserStatsProjection statsProjection = mock(UserStatsProjection.class);
        
        when(statsProjection.getTotalTournaments()).thenReturn(10);
        when(statsProjection.getTotalStrikes()).thenReturn(50);
        when(statsProjection.getAvgScore()).thenReturn(185.5);
        when(statsProjection.getBestGame()).thenReturn(245);
        when(statsProjection.getTournamentsWon()).thenReturn(3);

        when(resultRepository.findStatsByUserId(userId)).thenReturn(statsProjection);

        // Act
        UserStatisticsDTO result = userTournamentService.getUserStatistics(userId);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalTournaments());
        assertEquals(50, result.getTotalStrikes());
        assertEquals(185.5, result.getAvgScore());
        assertEquals(245, result.getBestGame());
        assertEquals(3, result.getTournamentsWon());
        verify(resultRepository).findStatsByUserId(userId);
    }

    @Test
    void getResultsForUserAndTournament_WithNullRound_ShouldHandleNullRound() {
        // Arrange
        Integer userId = 1;
        Integer tournamentId = 1;

        Result result = Result.builder()
                .resultId(1)
                .score(180)
                .round(null)  // Null round
                .laneNumber(1)
                .lineNumber(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(resultRepository.findResultsByPersonAndTournament(userId, tournamentId))
                .thenReturn(Arrays.asList(result));

        // Act
        List<UserTournamentResultDTO> results = userTournamentService
                .getResultsForUserAndTournament(userId, tournamentId);

        // Assert
        assertEquals(1, results.size());
        UserTournamentResultDTO firstResult = results.get(0);
        assertEquals(1, firstResult.getResultId());
        assertEquals(180, firstResult.getScore());
        assertNull(firstResult.getRonda());
        assertEquals(1, firstResult.getLaneNumber());
        assertEquals(1, firstResult.getLineNumber());

        verify(resultRepository).findResultsByPersonAndTournament(userId, tournamentId);
    }
}