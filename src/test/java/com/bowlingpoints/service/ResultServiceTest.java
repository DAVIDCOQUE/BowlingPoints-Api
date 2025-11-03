package com.bowlingpoints.service;

import com.bowlingpoints.dto.ResultDTO;
import com.bowlingpoints.dto.PlayerResultTableDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock private ResultRepository resultRepository;
    @Mock private PersonRepository personRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TournamentRepository tournamentRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ModalityRepository modalityRepository;
    @Mock private BranchRepository branchRepository;

    @InjectMocks
    private ResultService resultService;

    private Result sampleResult;
    private Tournament tournament;
    private Person person;
    private Category category;
    private Modality modality;
    private Branch branch;

    @BeforeEach
    void setUp() {
        tournament = new Tournament();
        tournament.setTournamentId(1);
        tournament.setName("Test Tournament");

        person = new Person();
        person.setPersonId(1);
        person.setFullName("John Doe");

        category = new Category();
        category.setCategoryId(1);
        category.setName("Open");

        modality = new Modality();
        modality.setModalityId(1);
        modality.setName("Individual");

        branch = new Branch();
        branch.setBranchId(1);
        branch.setName("Masculino");

        sampleResult = new Result();
        sampleResult.setResultId(1);
        sampleResult.setPerson(person);
        sampleResult.setTournament(tournament);
        sampleResult.setCategory(category);
        sampleResult.setModality(modality);
        sampleResult.setBranch(branch);
        sampleResult.setRoundNumber(1);
        sampleResult.setLaneNumber(3);
        sampleResult.setLineNumber(2);
        sampleResult.setScore(210);
    }

    // -------------------------------------------------
    // CRUD BÁSICO
    // -------------------------------------------------

    @Test
    void getAll_ShouldReturnListOfDTOs() {
        when(resultRepository.findAll()).thenReturn(List.of(sampleResult));

        List<ResultDTO> result = resultService.getAll();

        assertEquals(1, result.size());
        assertEquals(sampleResult.getResultId(), result.get(0).getResultId());
        verify(resultRepository, times(1)).findAll();
    }

    @Test
    void getById_ShouldReturnDTO_WhenExists() {
        when(resultRepository.findById(1)).thenReturn(Optional.of(sampleResult));

        ResultDTO dto = resultService.getById(1);

        assertNotNull(dto);
        assertEquals(sampleResult.getResultId(), dto.getResultId());
        assertEquals("John Doe", dto.getPersonName());
    }

    @Test
    void getById_ShouldReturnNull_WhenNotFound() {
        when(resultRepository.findById(999)).thenReturn(Optional.empty());

        ResultDTO dto = resultService.getById(999);

        assertNull(dto);
    }

    @Test
    void delete_ShouldReturnTrue_WhenExists() {
        when(resultRepository.existsById(1)).thenReturn(true);

        boolean deleted = resultService.delete(1);

        assertTrue(deleted);
        verify(resultRepository).deleteById(1);
    }

    @Test
    void delete_ShouldReturnFalse_WhenNotExists() {
        when(resultRepository.existsById(1)).thenReturn(false);

        boolean deleted = resultService.delete(1);

        assertFalse(deleted);
        verify(resultRepository, never()).deleteById(any());
    }

    // -------------------------------------------------
    // FILTRO AVANZADO
    // -------------------------------------------------

    @Test
    void getResultsByTournamentFiltered_ShouldFilterCorrectly() {
        Tournament t2 = new Tournament();
        t2.setTournamentId(2);
        Result r2 = new Result();
        r2.setTournament(t2);
        r2.setScore(150);

        when(resultRepository.findAll()).thenReturn(List.of(sampleResult, r2));

        List<ResultDTO> result = resultService.getResultsByTournamentFiltered(1, null, null);

        assertEquals(1, result.size());
        assertEquals(sampleResult.getScore(), result.get(0).getScore());
    }

    // -------------------------------------------------
    // CREATE / UPDATE
    // -------------------------------------------------

    @Test
    void create_ShouldMapDtoAndSaveEntity() {
        ResultDTO dto = ResultDTO.builder()
                .personId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .branchId(1)
                .roundNumber(1)
                .laneNumber(2)
                .lineNumber(3)
                .score(190)
                .build();

        when(personRepository.findById(1)).thenReturn(Optional.of(person));
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));
        when(branchRepository.findByBranchIdAndStatusTrue(1)).thenReturn(Optional.of(branch));
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultDTO saved = resultService.create(dto);

        assertNotNull(saved);
        verify(resultRepository, times(1)).save(any(Result.class));
    }

    @Test
    void update_ShouldReturnTrue_WhenEntityExists() {
        ResultDTO dto = ResultDTO.builder()
                .personId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .branchId(1)
                .roundNumber(2)
                .laneNumber(4)
                .lineNumber(5)
                .score(220)
                .build();

        when(resultRepository.findById(1)).thenReturn(Optional.of(sampleResult));
        when(personRepository.findById(1)).thenReturn(Optional.of(person));
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));
        when(branchRepository.findByBranchIdAndStatusTrue(1)).thenReturn(Optional.of(branch));

        boolean updated = resultService.update(1, dto);

        assertTrue(updated);
        verify(resultRepository).save(any(Result.class));
    }

    @Test
    void update_ShouldReturnFalse_WhenEntityNotFound() {
        when(resultRepository.findById(1)).thenReturn(Optional.empty());

        boolean updated = resultService.update(1, new ResultDTO());

        assertFalse(updated);
        verify(resultRepository, never()).save(any());
    }

    // -------------------------------------------------
    // getPlayerResultsForTable
    // -------------------------------------------------

    @Test
    void getPlayerResultsForTable_ShouldAggregateScoresAndCalculateAverages() {
        // row: personId, playerName, clubName, round, score, teamId, teamName
        List<Object[]> mockRows = List.of(
                new Object[]{1, "Alice", "Club A", 1, 200, null, null},
                new Object[]{1, "Alice", "Club A", 2, 180, null, null},
                new Object[]{2, "TeamPlayer", "Club B", 1, 210, 5, "Team Bravo"}
        );

        when(resultRepository.findRawPlayerResultsForTable(1, 1)).thenReturn(mockRows);

        List<PlayerResultTableDTO> result = resultService.getPlayerResultsForTable(1, 1, null);

        assertEquals(2, result.size());

        PlayerResultTableDTO alice = result.stream()
                .filter(r -> "Alice".equals(r.getPlayerName()))
                .findFirst().orElseThrow();

        assertEquals(380, alice.getTotal());
        assertEquals(190.0, alice.getPromedio(), 0.01);

        PlayerResultTableDTO team = result.stream()
                .filter(r -> "Team Bravo".equals(r.getTeamName()))
                .findFirst().orElseThrow();

        assertEquals(210, team.getTotal());
    }

    @Test
    void getPlayerResultsForTable_ShouldFilterByRoundNumber() {
        List<Object[]> mockRows = List.of(
                new Object[]{1, "Alice", "Club A", 1, 200, null, null},
                new Object[]{1, "Alice", "Club A", 2, 180, null, null}
        );

        when(resultRepository.findRawPlayerResultsForTable(1, 1)).thenReturn(mockRows);

        List<PlayerResultTableDTO> result = resultService.getPlayerResultsForTable(1, 1, 1);

        assertEquals(1, result.size());
        assertEquals(200, result.get(0).getTotal());
    }
}
