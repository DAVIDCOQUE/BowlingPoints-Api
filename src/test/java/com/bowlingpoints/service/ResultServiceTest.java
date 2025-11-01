package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para ResultService.
 */
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
    private CategoryRepository categoryRepository;
    @Mock
    private ModalityRepository modalityRepository;

    @InjectMocks
    private ResultService resultService;

    private Result result;
    private ResultDTO dto;
    private Person person;
    private Team team;
    private Tournament tournament;
    private Category category;
    private Modality modality;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        person = Person.builder().personId(1).fullName("John").gender("M").build();
        team = Team.builder().teamId(1).nameTeam("Team A").build();
        tournament = Tournament.builder().tournamentId(1).name("Open Cali").build();
        category = Category.builder().categoryId(1).name("Elite").build();
        modality = Modality.builder().modalityId(1).name("Individual").build();

        result = Result.builder()
                .resultId(1)
                .person(person)
                .tournament(tournament)
                .category(category)
                .modality(modality)
                .roundNumber(1)
                .laneNumber(3)
                .lineNumber(1)
                .score(250)
                .rama("M")
                .build();

        dto = ResultDTO.builder()
                .personId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .roundNumber(1)
                .laneNumber(3)
                .lineNumber(1)
                .score(250)
                .rama("M")
                .build();
    }

    // ----------------------------------------------------------------------
    // getAll / getById
    // ----------------------------------------------------------------------
    @Test
    void getAll_ShouldReturnMappedResults() {
        when(resultRepository.findAll()).thenReturn(List.of(result));

        List<ResultDTO> list = resultService.getAll();

        assertEquals(1, list.size());
        assertEquals("John", list.get(0).getPersonName());
        assertEquals(250, list.get(0).getScore());
    }

    @Test
    void getById_ShouldReturnMappedResult_WhenExists() {
        when(resultRepository.findById(1)).thenReturn(Optional.of(result));

        ResultDTO dto = resultService.getById(1);

        assertNotNull(dto);
        assertEquals("John", dto.getPersonName());
    }

    @Test
    void getById_ShouldReturnNull_WhenNotFound() {
        when(resultRepository.findById(99)).thenReturn(Optional.empty());

        ResultDTO dto = resultService.getById(99);

        assertNull(dto);
    }

    // ----------------------------------------------------------------------
    // create
    // ----------------------------------------------------------------------
    @Test
    void create_ShouldSaveResult_WhenValidDTO() {
        when(personRepository.findById(1)).thenReturn(Optional.of(person));
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));
        when(resultRepository.save(any(Result.class))).thenReturn(result);

        ResultDTO saved = resultService.create(dto);

        assertNotNull(saved);
        assertEquals("John", saved.getPersonName());
        verify(resultRepository).save(any(Result.class));
    }

    @Test
    void create_ShouldThrowException_WhenBothPersonAndTeamProvided() {
        ResultDTO invalid = ResultDTO.builder()
                .personId(1)
                .teamId(2)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .build();

        assertThrows(IllegalArgumentException.class, () -> resultService.create(invalid));
    }

    // ----------------------------------------------------------------------
    // update
    // ----------------------------------------------------------------------
    @Test
    void update_ShouldReturnTrue_WhenExists() {
        when(resultRepository.findById(1)).thenReturn(Optional.of(result));
        when(personRepository.findById(1)).thenReturn(Optional.of(person));
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));

        boolean ok = resultService.update(1, dto);

        assertTrue(ok);
        verify(resultRepository, times(1)).save(any(Result.class));
    }

    @Test
    void update_ShouldReturnFalse_WhenNotFound() {
        when(resultRepository.findById(99)).thenReturn(Optional.empty());

        boolean ok = resultService.update(99, dto);

        assertFalse(ok);
        verify(resultRepository, never()).save(any(Result.class));
    }

    // ----------------------------------------------------------------------
    // delete
    // ----------------------------------------------------------------------
    @Test
    void delete_ShouldRemove_WhenExists() {
        when(resultRepository.existsById(1)).thenReturn(true);

        boolean ok = resultService.delete(1);

        assertTrue(ok);
        verify(resultRepository).deleteById(1);
    }

    @Test
    void delete_ShouldReturnFalse_WhenNotFound() {
        when(resultRepository.existsById(99)).thenReturn(false);

        boolean ok = resultService.delete(99);

        assertFalse(ok);
        verify(resultRepository, never()).deleteById(any());
    }

    // ----------------------------------------------------------------------
    // getTournamentResultsByGender
    // ----------------------------------------------------------------------
    @Test
    void getTournamentResultsByGender_ShouldGroupResultsCorrectly() {
        List<Object[]> mockRows = List.of(
                new Object[]{1, "John", "M", 1, "Individual", 500, 250.0, 2},
                new Object[]{1, "John", "M", 2, "Parejas", 400, 200.0, 2},
                new Object[]{2, "Jane", "F", 1, "Individual", 300, 150.0, 2}
        );

        when(resultRepository.findPlayerModalitySummariesByTournament(1)).thenReturn(mockRows);

        Map<String, List<PlayerResultSummaryDTO>> map = resultService.getTournamentResultsByGender(1);

        assertEquals(2, map.size());
        assertTrue(map.containsKey("m"));
        assertTrue(map.containsKey("f"));
        assertEquals(1, map.get("f").size());
        assertEquals("John", map.get("m").get(0).getPlayerName());
        assertEquals(900, map.get("m").get(0).getTotalGlobal());
    }

    @Test
    void getTournamentResultsByGender_ShouldReturnEmptyMap_WhenNoResults() {
        when(resultRepository.findPlayerModalitySummariesByTournament(1)).thenReturn(List.of());

        Map<String, List<PlayerResultSummaryDTO>> map = resultService.getTournamentResultsByGender(1);

        assertTrue(map.isEmpty());
    }

    // ----------------------------------------------------------------------
    // getPlayerResultsForTable
    // ----------------------------------------------------------------------
    @Test
    void getPlayerResultsForTable_ShouldAggregateScoresByPlayer() {
        List<Object[]> mockData = List.of(
                new Object[]{1, "John", "Strike Club", 1, 200},
                new Object[]{1, "John", "Strike Club", 2, 250},
                new Object[]{2, "Jane", "Bowling Pro", 1, 180}
        );

        when(resultRepository.findRawPlayerResultsForTable(1, 1)).thenReturn(mockData);

        List<PlayerResultTableDTO> list = resultService.getPlayerResultsForTable(1, 1);

        assertEquals(2, list.size());
        PlayerResultTableDTO john = list.stream().filter(p -> p.getPersonId() == 1).findFirst().get();
        assertEquals(450, john.getTotal());
        assertEquals(225.0, john.getPromedio());
        assertEquals(2, john.getScores().size());
    }

    @Test
    void getPlayerResultsForTable_ShouldHandleEmptyList() {
        when(resultRepository.findRawPlayerResultsForTable(1, 1)).thenReturn(List.of());

        List<PlayerResultTableDTO> list = resultService.getPlayerResultsForTable(1, 1);

        assertTrue(list.isEmpty());
    }

    // ----------------------------------------------------------------------
    // getAllPlayersByAvgScore
    // ----------------------------------------------------------------------
    @Test
    void getAllPlayersByAvgScore_ShouldReturnPlayers() {
        DashboardPlayerDTO player = DashboardPlayerDTO.builder()
                .fullName("John")
                .averageScore(220.0)
                .build();

        when(resultRepository.findAllPlayersByAvgScore()).thenReturn(List.of(player));

        List<DashboardPlayerDTO> result = resultService.getAllPlayersByAvgScore();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFullName());
    }
}
