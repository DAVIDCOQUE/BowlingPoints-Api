package com.bowlingpoints.service;

import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultImportServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModalityRepository modalityRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ResultRepository resultRepository;

    @InjectMocks
    private ResultImportService resultImportService;

    private Person mockPerson;
    private Tournament mockTournament;
    private Category mockCategory;
    private Modality mockModality;
    private Branch mockBranch;
    private Team mockTeam;

    @BeforeEach
    void setUp() {
        mockPerson = Person.builder().personId(1).document("1234567").fullName("Juan").fullSurname("Pérez").build();
        mockTournament = Tournament.builder().tournamentId(1).name("Torneo Nacional 2026").build();
        mockCategory = Category.builder().categoryId(1).name("Senior").build();
        mockModality = Modality.builder().modalityId(1).name("Individual").build();
        mockBranch = Branch.builder().branchId(1).name("Masculina").build();
        mockTeam = Team.builder().teamId(1).nameTeam("Equipo A").build();
    }

    @Test
    void testImportCsv_Success() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Senior,Individual,Masculina,,1,5,1,245
                1234567,Torneo Nacional 2026,Senior,Individual,Masculina,,1,5,2,268
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));
        when(personRepository.findByDocument("1234567")).thenReturn(Optional.of(mockPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Senior")).thenReturn(Optional.of(mockCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Individual")).thenReturn(Optional.of(mockModality));
        when(branchRepository.findByNameIgnoreCase("Masculina")).thenReturn(Optional.of(mockBranch));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(false);
        when(resultRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(2, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());
        verify(resultRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testImportCsv_MultipleTournaments_ShouldFail() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Senior,Individual,Masculina,,1,5,1,245
                1234567,Torneo Regional 2026,Senior,Individual,Masculina,,1,5,2,268
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).contains("mismo torneo"));
        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void testImportCsv_PersonNotFound() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                9999999,Torneo Nacional 2026,Senior,Individual,Masculina,,1,5,1,245
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));
        when(personRepository.findByDocument("9999999")).thenReturn(Optional.empty());

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("no existe jugador")));
    }

    @Test
    void testImportCsv_TournamentNotFound() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Inexistente,Senior,Individual,Masculina,,1,5,1,245
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("No existe torneo")));
    }

    @Test
    void testImportCsv_CategoryNotFound() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Inexistente,Individual,Masculina,,1,5,1,245
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));
        when(personRepository.findByDocument("1234567")).thenReturn(Optional.of(mockPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("no existe categoría")));
    }

    @Test
    void testImportCsv_DuplicateResult_ShouldSkip() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Senior,Individual,Masculina,,1,5,1,245
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));
        when(personRepository.findByDocument("1234567")).thenReturn(Optional.of(mockPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Senior")).thenReturn(Optional.of(mockCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Individual")).thenReturn(Optional.of(mockModality));
        when(branchRepository.findByNameIgnoreCase("Masculina")).thenReturn(Optional.of(mockBranch));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("duplicado")));
        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void testImportCsv_WithTeam_Success() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Senior,Dobles,Femenina,Equipo A,1,3,1,190
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));
        when(personRepository.findByDocument("1234567")).thenReturn(Optional.of(mockPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Senior")).thenReturn(Optional.of(mockCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Dobles")).thenReturn(Optional.of(mockModality));
        when(branchRepository.findByNameIgnoreCase("Femenina")).thenReturn(Optional.of(mockBranch));
        when(teamRepository.findByNameTeam("Equipo A")).thenReturn(Optional.of(mockTeam));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(false);
        when(resultRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void testImportCsv_InvalidScore_ShouldFail() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Senior,Individual,Masculina,,1,5,1,350
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));
        when(personRepository.findByDocument("1234567")).thenReturn(Optional.of(mockPerson));

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("puntaje fuera de rango")));
    }

    @Test
    void testImportCsv_InvalidNumericFields() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Senior,Individual,Masculina,,ABC,5,1,245
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("no es un número válido")));
    }

    @Test
    void testImportCsv_EmptyFile() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).contains("No se encontraron filas"));
    }

    @Test
    void testImportCsv_InsufficientColumns() {
        // Arrange
        String csvContent = """
                documento,nombre_torneo,categoria,modalidad,rama,equipo,numero_ronda,numero_carril,numero_linea,puntaje
                1234567,Torneo Nacional 2026,Senior,Individual
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "results.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(tournamentRepository.findByName("Torneo Nacional 2026")).thenReturn(Optional.of(mockTournament));

        // Act
        var result = resultImportService.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("se esperaban 10 columnas")));
    }
}
