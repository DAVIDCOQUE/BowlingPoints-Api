package com.bowlingpoints.service;

import com.bowlingpoints.dto.RoundDTO;
import com.bowlingpoints.entity.Round;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.RoundRepository;
import com.bowlingpoints.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @InjectMocks
    private RoundService roundService;

    private Tournament testTournament;
    private Round testRound;
    private RoundDTO testRoundDTO;

    @BeforeEach
    void setUp() {
        // Configurar Tournament de prueba
        testTournament = Tournament.builder()
                .tournamentId(1)
                .name("Test Tournament")
                .build();

        // Configurar Round de prueba
        testRound = Round.builder()
                .roundId(1)
                .tournament(testTournament)
                .roundNumber(1)
                .build();

        // Configurar RoundDTO de prueba
        testRoundDTO = RoundDTO.builder()
                .roundId(1)
                .tournamentId(testTournament.getTournamentId())
                .roundNumber(1)
                .build();
    }

    @Test
    void getAll_WhenRoundsExist_ShouldReturnAllRounds() {
        // Arrange
        Round round2 = Round.builder()
                .roundId(2)
                .tournament(testTournament)
                .roundNumber(2)
                .build();
        
        when(roundRepository.findAll())
                .thenReturn(Arrays.asList(testRound, round2));

        // Act
        List<RoundDTO> results = roundService.getAll();

        // Assert
        assertEquals(2, results.size());
        
        RoundDTO firstRound = results.get(0);
        assertEquals(testRound.getRoundId(), firstRound.getRoundId());
        assertEquals(testRound.getRoundNumber(), firstRound.getRoundNumber());
        assertEquals(testTournament.getTournamentId(), firstRound.getTournamentId());

        RoundDTO secondRound = results.get(1);
        assertEquals(round2.getRoundId(), secondRound.getRoundId());
        assertEquals(round2.getRoundNumber(), secondRound.getRoundNumber());
    }

    @Test
    void getAll_WhenNoRounds_ShouldReturnEmptyList() {
        // Arrange
        when(roundRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<RoundDTO> results = roundService.getAll();

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void getById_WhenRoundExists_ShouldReturnRoundDTO() {
        // Arrange
        when(roundRepository.findById(1)).thenReturn(Optional.of(testRound));

        // Act
        RoundDTO result = roundService.getById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testRound.getRoundId(), result.getRoundId());
        assertEquals(testRound.getRoundNumber(), result.getRoundNumber());
        assertEquals(testTournament.getTournamentId(), result.getTournamentId());
    }

    @Test
    void getById_WhenRoundDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(roundRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        RoundDTO result = roundService.getById(99);

        // Assert
        assertNull(result);
    }

    @Test
    void create_WhenValidDTO_ShouldReturnCreatedRoundDTO() {
        // Arrange
        when(tournamentRepository.findById(testTournament.getTournamentId()))
                .thenReturn(Optional.of(testTournament));
        when(roundRepository.save(any(Round.class))).thenReturn(testRound);

        RoundDTO newRoundDTO = RoundDTO.builder()
                .tournamentId(testTournament.getTournamentId())
                .roundNumber(1)
                .build();

        // Act
        RoundDTO result = roundService.create(newRoundDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testRound.getRoundId(), result.getRoundId());
        assertEquals(testRound.getRoundNumber(), result.getRoundNumber());
        assertEquals(testTournament.getTournamentId(), result.getTournamentId());
    }

    @Test
    void create_WhenTournamentDoesNotExist_ShouldThrowException() {
        // Arrange
        when(tournamentRepository.findById(99))
                .thenReturn(Optional.empty());

        RoundDTO newRoundDTO = RoundDTO.builder()
                .tournamentId(99)
                .roundNumber(1)
                .build();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> roundService.create(newRoundDTO));
        verify(roundRepository, never()).save(any());
    }

    @Test
    void update_WhenRoundExists_ShouldReturnTrue() {
        // Arrange
        when(roundRepository.findById(1)).thenReturn(Optional.of(testRound));
        when(tournamentRepository.findById(testTournament.getTournamentId()))
                .thenReturn(Optional.of(testTournament));

        RoundDTO updateDTO = RoundDTO.builder()
                .roundId(1)
                .tournamentId(testTournament.getTournamentId())
                .roundNumber(2)
                .build();

        // Act
        boolean result = roundService.update(1, updateDTO);

        // Assert
        assertTrue(result);
        verify(roundRepository).save(argThat(round ->
                round.getRoundNumber() == 2 &&
                round.getTournament().getTournamentId().equals(testTournament.getTournamentId())
        ));
    }

    @Test
    void update_WhenRoundDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(roundRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        boolean result = roundService.update(99, testRoundDTO);

        // Assert
        assertFalse(result);
        verify(roundRepository, never()).save(any());
    }

    @Test
    void update_WhenTournamentDoesNotExist_ShouldThrowException() {
        // Arrange
        when(roundRepository.findById(1)).thenReturn(Optional.of(testRound));
        when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

        RoundDTO updateDTO = RoundDTO.builder()
                .roundId(1)
                .tournamentId(99)
                .roundNumber(1)
                .build();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> roundService.update(1, updateDTO));
        verify(roundRepository, never()).save(any());
    }

    @Test
    void delete_WhenRoundExists_ShouldReturnTrue() {
        // Arrange
        when(roundRepository.existsById(1)).thenReturn(true);

        // Act
        boolean result = roundService.delete(1);

        // Assert
        assertTrue(result);
        verify(roundRepository).deleteById(1);
    }

    @Test
    void delete_WhenRoundDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(roundRepository.existsById(99)).thenReturn(false);

        // Act
        boolean result = roundService.delete(99);

        // Assert
        assertFalse(result);
        verify(roundRepository, never()).deleteById(any());
    }
}