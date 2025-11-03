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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
    private CategoryRepository categoryRepository;
    @Mock
    private ModalityRepository modalityRepository;
    @Mock
    private BranchRepository branchRepository;

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


    @Test
    void update_ShouldSkip_WhenBranchNotFound() {
        ResultDTO dto = ResultDTO.builder()
                .personId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .branchId(99)
                .build();

        when(resultRepository.findById(1)).thenReturn(Optional.of(sampleResult));
        when(personRepository.findById(1)).thenReturn(Optional.of(person));
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(modalityRepository.findById(1)).thenReturn(Optional.of(modality));
        when(branchRepository.findByBranchIdAndStatusTrue(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> resultService.update(1, dto));
    }


    @Test
    void getResultsByTournamentFiltered_ShouldApplyAllFilters() {
        Result r1 = sampleResult;
        Result r2 = new Result();
        r2.setTournament(tournament);
        Category c2 = new Category();
        c2.setCategoryId(2);
        r2.setCategory(c2);

        when(resultRepository.findAll()).thenReturn(List.of(r1, r2));

        List<ResultDTO> result = resultService.getResultsByTournamentFiltered(1, 1, null);

        assertEquals(1, result.size());
    }

    @Test
    void getPlayerResultsForTable_ShouldHandleEmptyData() {
        when(resultRepository.findRawPlayerResultsForTable(1, 1)).thenReturn(Collections.emptyList());
        List<PlayerResultTableDTO> result = resultService.getPlayerResultsForTable(1, 1, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void delete_ShouldHandleException() {
        when(resultRepository.existsById(1)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(resultRepository).deleteById(1);

        assertThrows(RuntimeException.class, () -> resultService.delete(1));
    }

    @Test
    void getResultsByModality_ShouldBuildResponseCorrectly() {

        List<Object[]> rows = List.<Object[]>of(
                new Object[]{1, "Player 1", "Club A", "Sencillos Masculino", 600L, 3L}
        );

        when(resultRepository.findPlayerTotalsByModalityAndBranch(1, 1, 1))
                .thenReturn(rows);

        Tournament tournament = new Tournament();
        tournament.setTournamentId(1);
        tournament.setName("Torneo Test");
        TournamentModality tm = new TournamentModality();
        tm.setModality(Modality.builder().modalityId(1).name("Sencillos Masculino").description("Desc").status(true).build());
        tournament.setModalities(List.of(tm));

        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(resultRepository.findDistinctRoundsByTournament(1)).thenReturn(List.of(1, 2));

        var result = resultService.getResultsByModality(1, 1, 1);

        assertNotNull(result.getTournament());
        assertEquals("Torneo Test", result.getTournament().getTournamentName());
        assertEquals(1, result.getResultsByModality().size());
        assertEquals("Player 1", result.getResultsByModality().get(0).getPlayerName());
        assertEquals(600, result.getResultsByModality().get(0).getTotal());
    }

    @Test
    void getTournamentResultsTable_ShouldReturnCompleteSummary() {
        List<Object[]> playerResults = List.<Object[]>of(
                new Object[]{1, "Alice", "Club A", 1, 200, null, null}
        );

        List<Object[]> avgByLine = List.<Object[]>of(
                new Object[]{"Alice", 200.0}
        );

        List<Object[]> highestLine = List.<Object[]>of(
                new Object[]{210, "Alice", 3}
        );

        when(resultRepository.findRawPlayerResultsForTable(1, 1))
                .thenReturn(playerResults);

        when(resultRepository.findDistinctRoundsByTournamentAndModality(1, 1))
                .thenReturn(List.of(1, 2));

        when(resultRepository.findAvgByLineRaw(1, 1, 1))
                .thenReturn(avgByLine);

        when(resultRepository.findAvgByRound(1, 1, 1))
                .thenReturn(190.0);

        when(resultRepository.findHighestLine(1, 1, 1))
                .thenReturn(highestLine);

        Tournament t = new Tournament();
        t.setTournamentId(1);
        t.setName("Torneo Test");
        TournamentModality tm = new TournamentModality();
        tm.setModality(Modality.builder()
                .modalityId(1)
                .name("Sencillos")
                .description("desc")
                .status(true)
                .build());
        t.setModalities(List.of(tm));

        when(tournamentRepository.findById(1)).thenReturn(Optional.of(t));

        var dto = resultService.getTournamentResultsTable(1, 1, 1);

        assertEquals("Torneo Test", dto.getTournament().getTournamentName());
        assertEquals(190.0, dto.getAvgByRound());
        assertEquals("Alice", dto.getHighestLine().getPlayerName());
    }

    @Test
    void getTournamentResultsByGender_ShouldGroupPlayersByGender() {
        List<Object[]> rows = List.<Object[]>of(
                new Object[]{1, "Player 1", "Masculino", 1, "Sencillos", 500, 5, 2}
        );

        when(resultRepository.findPlayerModalitySummariesByTournament(1))
                .thenReturn(rows);

        Map<String, List<com.bowlingpoints.dto.PlayerResultSummaryDTO>> result =
                resultService.getTournamentResultsByGender(1);

        assertTrue(result.containsKey("masculino"));
        var list = result.get("masculino");
        assertEquals(1, list.size());
        assertEquals("Player 1", list.get(0).getPlayerName());
        assertEquals(500, list.get(0).getTotalGlobal());
    }


    @Test
    void mapEntityToDto_ShouldHandleNullFieldsGracefully() {
        Result r = new Result();
        r.setResultId(99); // sin persona, sin equipo, etc.

        // Llamar por reflexión o hacerlo público para test
        var method = Arrays.stream(ResultService.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("mapEntityToDto"))
                .findFirst().orElseThrow();
        method.setAccessible(true);

        ResultDTO dto = assertDoesNotThrow(() -> (ResultDTO) method.invoke(resultService, r));
        assertEquals(99, dto.getResultId());
        assertNull(dto.getPersonName());
        assertNull(dto.getTeamName());
    }

    @Test
    void mapDtoToEntity_ShouldThrow_WhenPersonAndTeamProvided() throws Exception {
        ResultDTO dto = ResultDTO.builder()
                .personId(1)
                .teamId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .build();

        var method = Arrays.stream(ResultService.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("mapDtoToEntity"))
                .findFirst().orElseThrow();
        method.setAccessible(true);

        Exception ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(resultService, dto, new Result()));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void mapDtoToEntity_ShouldThrow_WhenTournamentNotFound() throws Exception {
        ResultDTO dto = ResultDTO.builder()
                .personId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .build();

        when(personRepository.findById(1)).thenReturn(Optional.of(person));
        when(tournamentRepository.findById(1)).thenReturn(Optional.empty());

        var method = Arrays.stream(ResultService.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("mapDtoToEntity"))
                .findFirst().orElseThrow();
        method.setAccessible(true);

        Exception ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(resultService, dto, new Result()));
        assertTrue(ex.getCause() instanceof RuntimeException);
    }

    @Test
    void getResultsByModality_ShouldHandleEmptyRepositories() {
        when(tournamentRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(resultRepository.findDistinctRoundsByTournament(anyInt())).thenReturn(Collections.emptyList());
        when(resultRepository.findPlayerTotalsByModalityAndBranch(anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        var result = resultService.getResultsByModality(1, 1, 1);

        assertNotNull(result);
        assertNull(result.getTournament());
        assertTrue(result.getResultsByModality().isEmpty());
    }

    @Test
    void getTournamentResultsTable_ShouldHandleEmptyData() {
        when(tournamentRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(resultRepository.findRawPlayerResultsForTable(anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(resultRepository.findDistinctRoundsByTournamentAndModality(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(resultRepository.findAvgByLineRaw(anyInt(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(resultRepository.findHighestLine(anyInt(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        var result = resultService.getTournamentResultsTable(1, 1, 1);
        assertNotNull(result);
        assertNull(result.getTournament());
    }

    @Test
    void mapDtoToEntity_ShouldThrow_WhenTeamNotFound() throws Exception {
        ResultDTO dto = ResultDTO.builder()
                .teamId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .build();

        when(teamRepository.findById(1)).thenReturn(Optional.empty());

        var method = Arrays.stream(ResultService.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("mapDtoToEntity"))
                .findFirst().orElseThrow();
        method.setAccessible(true);

        Exception ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(resultService, dto, new Result()));
        assertTrue(ex.getCause() instanceof RuntimeException);
        assertTrue(ex.getCause().getMessage().contains("Equipo no encontrado"));
    }

    @Test
    void mapDtoToEntity_ShouldThrow_WhenCategoryNotFound() throws Exception {
        ResultDTO dto = ResultDTO.builder()
                .personId(1)
                .tournamentId(1)
                .categoryId(1)
                .modalityId(1)
                .build();

        when(personRepository.findById(1)).thenReturn(Optional.of(person));
        when(tournamentRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        var method = Arrays.stream(ResultService.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("mapDtoToEntity"))
                .findFirst().orElseThrow();
        method.setAccessible(true);

        Exception ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(resultService, dto, new Result()));
        assertTrue(ex.getCause() instanceof RuntimeException);
        assertTrue(ex.getCause().getMessage().contains("Categoría no encontrada"));
    }

    @Test
    void getResultsByModality_ShouldHandleEmptyDataGracefully() {
        when(tournamentRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(resultRepository.findDistinctRoundsByTournament(anyInt())).thenReturn(Collections.emptyList());
        when(resultRepository.findPlayerTotalsByModalityAndBranch(anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        var dto = resultService.getResultsByModality(1, 1, 1);

        assertNotNull(dto);
        assertNull(dto.getTournament());
        assertTrue(dto.getResultsByModality().isEmpty());
    }

}
