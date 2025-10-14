package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private RoundRepository roundRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ModalityRepository modalityRepository;

    @InjectMocks
    private ResultService resultService;

    private Result testResult;
    private ResultDTO testResultDTO;
    private Person testPerson;
    private Team testTeam;
    private Tournament testTournament;
    private Round testRound;
    private Category testCategory;
    private Modality testModality;

    @BeforeEach
    void setUp() {
        // Configurar entidades de prueba
        testPerson = Person.builder()
                .personId(1)
                .fullName("John Doe")
                .build();

        testTeam = Team.builder()
                .teamId(1)
                .nameTeam("Test Team")
                .build();

        testTournament = Tournament.builder()
                .tournamentId(1)
                .name("Test Tournament")
                .build();

        testRound = Round.builder()
                .roundId(1)
                .roundNumber(1)
                .tournament(testTournament)
                .build();

        testCategory = Category.builder()
                .categoryId(1)
                .name("Test Category")
                .build();

        testModality = Modality.builder()
                .modalityId(1)
                .name("Test Modality")
                .build();

        testResult = Result.builder()
                .resultId(1)
                .person(testPerson)
                .tournament(testTournament)
                .round(testRound)
                .category(testCategory)
                .modality(testModality)
                .laneNumber(1)
                .lineNumber(1)
                .score(180)
                .build();

        testResultDTO = ResultDTO.builder()
                .resultId(1)
                .personId(testPerson.getPersonId())
                .personName(testPerson.getFullName())
                .tournamentId(testTournament.getTournamentId())
                .tournamentName(testTournament.getName())
                .roundId(testRound.getRoundId())
                .roundNumber(testRound.getRoundNumber())
                .categoryId(testCategory.getCategoryId())
                .categoryName(testCategory.getName())
                .modalityId(testModality.getModalityId())
                .modalityName(testModality.getName())
                .laneNumber(1)
                .lineNumber(1)
                .score(180)
                .build();
    }

    @Test
    void getAll_ShouldReturnAllResults() {
        // Arrange
        when(resultRepository.findAll()).thenReturn(List.of(testResult));

        // Act
        List<ResultDTO> results = resultService.getAll();

        // Assert
        assertEquals(1, results.size());
        ResultDTO resultDTO = results.get(0);
        assertEquals(testResult.getResultId(), resultDTO.getResultId());
        assertEquals(testResult.getPerson().getPersonId(), resultDTO.getPersonId());
        assertEquals(testResult.getScore(), resultDTO.getScore());
    }

    @Test
    void getById_WhenResultExists_ShouldReturnResultDTO() {
        // Arrange
        when(resultRepository.findById(1)).thenReturn(Optional.of(testResult));

        // Act
        ResultDTO result = resultService.getById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testResult.getResultId(), result.getResultId());
        assertEquals(testResult.getScore(), result.getScore());
    }

    @Test
    void getById_WhenResultDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(resultRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        ResultDTO result = resultService.getById(99);

        // Assert
        assertNull(result);
    }

    @Test
    void create_WhenValidDTO_ShouldReturnCreatedResultDTO() {
        // Arrange
        when(personRepository.findById(testPerson.getPersonId())).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findById(testTournament.getTournamentId())).thenReturn(Optional.of(testTournament));
        when(roundRepository.findById(testRound.getRoundId())).thenReturn(Optional.of(testRound));
        when(categoryRepository.findById(testCategory.getCategoryId())).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findById(testModality.getModalityId())).thenReturn(Optional.of(testModality));
        when(resultRepository.save(any(Result.class))).thenReturn(testResult);

        // Act
        ResultDTO result = resultService.create(testResultDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testResult.getScore(), result.getScore());
        assertEquals(testResult.getPerson().getPersonId(), result.getPersonId());
    }

    @Test
    void update_WhenResultExists_ShouldReturnTrue() {
        // Arrange
        when(resultRepository.findById(1)).thenReturn(Optional.of(testResult));
        when(personRepository.findById(testPerson.getPersonId())).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findById(testTournament.getTournamentId())).thenReturn(Optional.of(testTournament));
        when(roundRepository.findById(testRound.getRoundId())).thenReturn(Optional.of(testRound));
        when(categoryRepository.findById(testCategory.getCategoryId())).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findById(testModality.getModalityId())).thenReturn(Optional.of(testModality));

        // Act
        boolean updated = resultService.update(1, testResultDTO);

        // Assert
        assertTrue(updated);
        verify(resultRepository).save(any(Result.class));
    }

    @Test
    void update_WhenResultDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(resultRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        boolean updated = resultService.update(99, testResultDTO);

        // Assert
        assertFalse(updated);
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void delete_WhenResultExists_ShouldReturnTrue() {
        // Arrange
        when(resultRepository.existsById(1)).thenReturn(true);

        // Act
        boolean deleted = resultService.delete(1);

        // Assert
        assertTrue(deleted);
        verify(resultRepository).deleteById(1);
    }

    @Test
    void delete_WhenResultDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(resultRepository.existsById(99)).thenReturn(false);

        // Act
        boolean deleted = resultService.delete(99);

        // Assert
        assertFalse(deleted);
        verify(resultRepository, never()).deleteById(any());
    }

    @Test
    void getTournamentResultsByGender_ShouldReturnGroupedResults() {
        // Arrange
        Object[] row = new Object[]{
                1,           // playerId
                "John Doe",  // playerName
                "masculino", // gender
                1,           // modalityId
                "Singles",   // modalityName
                900,         // total
                180.0,       // average
                5            // lines
        };

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(row);

        when(resultRepository.findPlayerModalitySummariesByTournament(1))
                .thenReturn(mockData);

        // Act
        Map<String, List<PlayerResultSummaryDTO>> results = resultService.getTournamentResultsByGender(1);

        // Assert
        assertNotNull(results);
        assertTrue(results.containsKey("masculino"));
        List<PlayerResultSummaryDTO> maleResults = results.get("masculino");
        assertEquals(1, maleResults.size());

        PlayerResultSummaryDTO summary = maleResults.get(0);
        assertEquals(1, summary.getPlayerId());
        assertEquals("John Doe", summary.getPlayerName());
        assertEquals(900, summary.getTotalGlobal());
        assertEquals(180.0, summary.getPromedioGlobal());
        assertEquals(5, summary.getLineasGlobal());
    }

    @Test
    void getPlayerResultsForTable_ShouldReturnFormattedResults() {
        // Arrange
        Object[] row = new Object[]{
                1, "John Doe", "Test Club", 1, 180
        };

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(row);

        when(resultRepository.findRawPlayerResultsForTable(1, 1))
                .thenReturn(mockData);

        // Act
        List<PlayerResultTableDTO> results = resultService.getPlayerResultsForTable(1, 1);

        // Assert
        assertEquals(1, results.size());
        PlayerResultTableDTO playerResult = results.get(0);
        assertEquals(1, playerResult.getPersonId());
        assertEquals("John Doe", playerResult.getPlayerName());
        assertEquals("Test Club", playerResult.getClubName());
        assertEquals(180, playerResult.getTotal());
        assertEquals(180.0, playerResult.getPromedio());
        assertEquals(1, playerResult.getScores().size());
        assertEquals(180, playerResult.getScores().get(0));
    }

    @Test
    void getAllPlayersByAvgScore_ShouldReturnSortedPlayers() {
        // Arrange
        PlayerRankingDTO ranking = PlayerRankingDTO.builder()
                .personId(1)
                .fullName("John Doe")
                .averageScore(180.0).build();
        when(resultRepository.findAllPlayersByAvgScore())
            .thenReturn(List.of(ranking));

        // Act
        List<PlayerRankingDTO> rankings = resultService.getAllPlayersByAvgScore();

        // Assert
        assertEquals(1, rankings.size());
        assertEquals(ranking.getPersonId(), rankings.get(0).getPersonId());
        assertEquals(ranking.getFullName(), rankings.get(0).getFullName());
        assertEquals(ranking.getAverageScore(), rankings.get(0).getAverageScore());
    }
}