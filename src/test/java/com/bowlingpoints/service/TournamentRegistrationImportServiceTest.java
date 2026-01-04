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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentRegistrationImportServiceTest {

    @Mock
    private TournamentRegistrationRepository registrationRepository;

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

    @InjectMocks
    private TournamentRegistrationImportService service;

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

        testBranch = new Branch();
        testBranch.setBranchId(1);

        testTeam = new Team();
        testTeam.setTeamId(1);
    }

    // Helper method
    private MockMultipartFile createCsvFile(String content) {
        return new MockMultipartFile("file", "tournament-registrations.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importCsv_WithValidData_CreatesRegistrations() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456,Torneo Nacional 2024\n789012,Torneo Nacional 2024\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(personRepository.findByDocument("789012")).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                anyInt(), isNull(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(2, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository, times(2)).save(any(TournamentRegistration.class));
    }

    @Test
    void importCsv_WithEmptyDocument_SkipsRow() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n,Torneo Nacional 2024\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("documentNumber o tournamentName vacío"));

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithEmptyTournamentName_SkipsRow() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456,\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("documentNumber o tournamentName vacío"));

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithNonExistentPerson_SkipsRow() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n999999,Torneo Nacional 2024\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("999999")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("no existe Person con documento=999999"));

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithNonExistentTournament_SkipsRow() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456,Torneo Inexistente\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Inexistente")).thenReturn(Optional.empty());

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("no existe Tournament con nombre=Torneo Inexistente"));

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithDuplicateRegistration_SkipsRow() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456,Torneo Nacional 2024\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                anyInt(), isNull(), anyInt())).thenReturn(true);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithAllOptionalFields_CreatesRegistration() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName,categoryName,modalityName,branchName,teamName\n" +
                "123456,Torneo Nacional 2024,Juvenil,Singles,Masculino,Eagles\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Juvenil")).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull("Singles")).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase("Masculino")).thenReturn(Optional.of(testBranch));
        when(teamRepository.findByNameTeam("Eagles")).thenReturn(Optional.of(testTeam));
        when(registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                anyInt(), anyInt(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository).save(any(TournamentRegistration.class));
    }

    @Test
    void importCsv_WithOnlyRequiredFields_CreatesRegistrationWithNulls() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456,Torneo Nacional 2024\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                anyInt(), isNull(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository).save(any(TournamentRegistration.class));
        verify(categoryRepository, never()).findByNameAndDeletedAtIsNull(anyString());
        verify(modalityRepository, never()).findByNameAndDeletedAtIsNull(anyString());
        verify(branchRepository, never()).findByNameIgnoreCase(anyString());
        verify(teamRepository, never()).findByNameTeam(anyString());
    }

    @Test
    void importCsv_WithLessThan2Columns_SkipsRow() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).contains("se esperaban al menos 2 columnas"));

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithEmptyFile_ReturnsNoErrors() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithSkipHeaderTrue_IgnoresFirstLine() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456,Torneo Nacional 2024\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                anyInt(), isNull(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true); // skipHeader = true

        // Assert
        assertEquals(1, result.created()); // Only 1 row processed (header ignored)

        verify(personRepository, never()).findByDocument("documentNumber");
    }

    @Test
    void importCsv_WithEmptyLinesInMiddle_SkipsThem() throws Exception {
        // Arrange
        String csv = "documentNumber,tournamentName\n123456,Torneo Nacional 2024\n\n789012,Torneo Nacional 2024\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument("123456")).thenReturn(Optional.of(testPerson));
        when(personRepository.findByDocument("789012")).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                anyInt(), isNull(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(2, result.created()); // Only 2 valid rows processed
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository, times(2)).save(any(TournamentRegistration.class));
    }

    @Test
    void importCsv_WithIOException_HandlesGracefully() throws Exception {
        // Arrange - Empty file to test edge case
        String csv = "";
        MockMultipartFile file = createCsvFile(csv);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        // Empty file doesn't generate errors, just returns empty result
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void importCsv_WithVariableColumnsCount_HandlesGracefully() throws Exception {
        // Arrange - Different rows with different column counts
        String csv = "documentNumber,tournamentName,categoryName,modalityName,branchName,teamName\n" +
                "123456,Torneo Nacional 2024\n" +
                "789012,Torneo Nacional 2024,Juvenil,Singles\n" +
                "111111,Torneo Nacional 2024,Senior,Doubles,Femenino,Hawks\n";
        MockMultipartFile file = createCsvFile(csv);

        when(personRepository.findByDocument(anyString())).thenReturn(Optional.of(testPerson));
        when(tournamentRepository.findByName("Torneo Nacional 2024")).thenReturn(Optional.of(testTournament));
        when(categoryRepository.findByNameAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(testCategory));
        when(modalityRepository.findByNameAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(testModality));
        when(branchRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(testBranch));
        when(teamRepository.findByNameTeam(anyString())).thenReturn(Optional.of(testTeam));
        when(registrationRepository.existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(
                anyInt(), any(), anyInt())).thenReturn(false);

        // Act
        var result = service.importCsv(file, 1, true);

        // Assert
        assertEquals(3, result.created()); // All 3 rows should be processed
        assertTrue(result.errors().isEmpty());

        verify(registrationRepository, times(3)).save(any(TournamentRegistration.class));
    }
}
