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
import java.util.ArrayList;
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
    private ResultImportService service;

    private Person testPerson;
    private Tournament testTournament;
    private Category testCategory;
    private Modality testModality;
    private Branch testBranch;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setPersonId(1);

        testTournament = new Tournament();
        testTournament.setTournamentId(1);

        testCategory = new Category();
        testCategory.setCategoryId(1);

        testModality = new Modality();
        testModality.setModalityId(1);
        testModality.setName("Dobles"); // Modalidad que requiere equipo

        testBranch = new Branch();
        testBranch.setBranchId(1);

        testTeam = new Team();
        testTeam.setTeamId(1);
    }

    private MockMultipartFile createCsvFile(String content) {
        return new MockMultipartFile("file", "results.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importCsv_WithValidData_CreatesResults() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,245\n" +
                "789012,Torneo Nacional 2024,Juvenil,Dobles,Femenino,Hawks,1,6,1,198\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument(anyString())).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Dobles")).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(testBranch));
        when(teamRepository.findByNameTeam(anyString())).thenReturn(Optional.of(testTeam));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(2, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(resultRepository, times(1)).saveAll(anyList());
    }

    @Test
    void importCsv_WithMultipleTournaments_ReturnsError() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,245\n" +
                "789012,Torneo Provincial 2024,Juvenil,Dobles,Femenino,Hawks,1,6,1,198\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("múltiples torneos"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithNonExistentTournament_ReturnsError() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Inexistente,Juvenil,Dobles,Masculino,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("No existe torneo"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithMissingRequiredFields_SkipsRows() throws Exception {
        // Arrange - documento vacío
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                ",Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("campos obligatorios vacíos"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithInvalidScore_SkipsRow() throws Exception {
        // Arrange - puntaje 350 (fuera de rango 0-300)
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,350\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("puntaje fuera de rango"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithNegativeRoundOrLane_SkipsRow() throws Exception {
        // Arrange - numeroRonda = 0
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,0,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("deben ser > 0"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithNonExistentPerson_SkipsRow() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "999999,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("999999")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("no existe jugador"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithNonExistentCategory_SkipsRow() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Inexistente,Dobles,Masculino,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("no existe categoría"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithNonExistentModality_SkipsRow() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Inexistente,Masculino,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("no existe modalidad"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithNonExistentBranch_SkipsRow() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Inexistente,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Dobles")).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase("Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("no existe rama"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithNonExistentTeam_SkipsRow() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Inexistente,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Dobles")).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase("Masculino")).thenReturn(Optional.of(testBranch));
        when(teamRepository.findByNameTeam("Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).contains("no existe equipo"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithEmptyTeam_CreatesResultWithoutTeam() throws Exception {
        // Arrange - equipo vacío (campo opcional para modalidades Individual/Sencillo)
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Sencillo Masculino,Masculino,,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        Modality individualModality = new Modality();
        individualModality.setModalityId(2);
        individualModality.setName("Sencillo Masculino"); // Modalidad que NO requiere equipo

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Sencillo Masculino")).thenReturn(Optional.of(individualModality));
        when(branchRepository.findByNameIgnoreCase("Masculino")).thenReturn(Optional.of(testBranch));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(teamRepository, never()).findByNameTeam(anyString());
        verify(resultRepository).saveAll(anyList());
    }

    @Test
    void importCsv_WithDuplicateResult_SkipsRow() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Dobles")).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase("Masculino")).thenReturn(Optional.of(testBranch));
        when(teamRepository.findByNameTeam("Eagles")).thenReturn(Optional.of(testTeam));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("resultado duplicado"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithBatchProcessing_SavesIn500Batches() throws Exception {
        // Arrange - Generate 1001 rows
        StringBuilder csvBuilder = new StringBuilder("documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n");
        for (int i = 1; i <= 1001; i++) {
            csvBuilder.append(String.format("DOC%d,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,%d,245\n", i, i));
        }
        MockMultipartFile file = createCsvFile(csvBuilder.toString());

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument(anyString())).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Dobles")).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase("Masculino")).thenReturn(Optional.of(testBranch));
        when(teamRepository.findByNameTeam("Eagles")).thenReturn(Optional.of(testTeam));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(1001, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        // Verify saveAll called 3 times: 500 + 500 + 1
        verify(resultRepository, times(3)).saveAll(anyList());
    }

    @Test
    void importCsv_WithParsingError_AddsError() throws Exception {
        // Arrange - invalid number format in puntaje
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,abc\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).contains("no es un número válido"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithLessThan10Columns_SkipsRow() throws Exception {
        // Arrange - only 5 columns
        String csv = "documento,nombreTorneo,categoria,modalidad,rama\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).contains("se esperaban 10 columnas"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithEmptyFile_ReturnsEmptyRowsError() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("No se encontraron filas válidas"));

        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_WithSkipHeaderTrue_IgnoresFirstLine() throws Exception {
        // Arrange
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123456,Torneo Nacional 2024,Juvenil,Dobles,Masculino,Eagles,1,5,1,245\n";
        MockMultipartFile file = createCsvFile(csv);

        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Dobles")).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase("Masculino")).thenReturn(Optional.of(testBranch));
        when(teamRepository.findByNameTeam("Eagles")).thenReturn(Optional.of(testTeam));
        when(resultRepository.existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
                anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true); // skipHeader = true

        // Assert
        assertEquals(1, result.created()); // Only 1 row processed (header ignored)

        verify(personRepository, never()).findByDocument("documento");
    }

    @Test
    void importCsv_WithIOException_HandlesGracefully() throws Exception {
        // Arrange - Create file with invalid input that will cause parsing errors
        String csv = "documento,nombreTorneo,categoria,modalidad,rama,equipo,numeroRonda,numeroCarril,numeroLinea,puntaje\n" +
                "123,Torneo,Cat,Mod,Rama,Equipo,invalid,invalid,invalid,invalid\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        // Verify there are parsing errors
        assertTrue(result.errors().size() > 0);

        verify(resultRepository, never()).saveAll(anyList());
    }
}
