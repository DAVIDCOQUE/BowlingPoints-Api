package com.bowlingpoints.service.impl;

import com.bowlingpoints.dto.PlayerResultUploadDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelServiceImplTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private ResultRepository resultRepository;
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ModalityRepository modalityRepository;

    @InjectMocks
    private com.bowlingpoints.service.impl.ExcelServiceImpl excelService;

    private MockMultipartFile mockFile;
    private Tournament tournament;
    private Category category;
    private Person person;

    @BeforeEach
    void setUp() throws Exception {
        // Crear un Excel en memoria
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");

        // Metadata
        sheet.createRow(0).createCell(0).setCellValue("Torneo Test");
        sheet.createRow(1).createCell(0).setCellValue("Modality A");
        sheet.createRow(2).createCell(0).setCellValue("Masculino");
        sheet.createRow(3).createCell(0).setCellValue("Categoría A");

        // Encabezados ficticios
        sheet.createRow(4); // Rondas
        Row headerRow = sheet.createRow(5);
        headerRow.createCell(0).setCellValue("Documento");
        headerRow.createCell(1).setCellValue("Nombre");
        headerRow.createCell(2).setCellValue("Apellido");
        headerRow.createCell(3).setCellValue("Club");
        headerRow.createCell(4).setCellValue("Línea 1");

        // Jugador
        Row playerRow = sheet.createRow(6);
        playerRow.createCell(0).setCellValue("123");
        playerRow.createCell(1).setCellValue("John");
        playerRow.createCell(2).setCellValue("Doe");
        playerRow.createCell(3).setCellValue("ABC Club");
        playerRow.createCell(4).setCellValue(250);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        mockFile = new MockMultipartFile("file", "results.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray());

        // Mocks de entidades
        tournament = Tournament.builder().tournamentId(1).name("Torneo Test").status(true).build();
        category = Category.builder().categoryId(1).name("Categoría A").build();
        person = new Person();
        person.setPersonId(1);
        person.setDocument("123");
        person.setFullName("John");
        person.setFullSurname("Doe");
        person.setEmail("john@bowlingpoints.com");
    }

    @Test
    void shouldUploadResultsFromExcelSuccessfully() {
        // Arrange
        when(tournamentRepository.findByName("Torneo Test")).thenReturn(Optional.of(tournament));
        when(categoryRepository.findByNameAndDeletedAtIsNull("Categoría A")).thenReturn(Optional.of(category));
        when(personRepository.findByDocument("123")).thenReturn(Optional.of(person));
        when(resultRepository.save(any(Result.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PlayerResultUploadDTO> result = excelService.uploadResultsFromExcel(mockFile);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
        assertThat(result.get(0).getTotalLines()).isGreaterThanOrEqualTo(0);

        verify(tournamentRepository).findByName("Torneo Test");
        verify(categoryRepository).findByNameAndDeletedAtIsNull("Categoría A");
        verify(personRepository).findByDocument("123");
        verify(resultRepository, atLeastOnce()).save(any(Result.class));
    }

    @Test
    void shouldHandleNewEntitiesWhenNotFound() {
        // Arrange: no existen entidades, se deben crear
        when(tournamentRepository.findByName(any())).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
        when(tournamentRepository.save(any())).thenReturn(tournament);
        when(categoryRepository.save(any())).thenReturn(category);
        when(personRepository.findByDocument(any())).thenReturn(Optional.empty());
        when(personRepository.save(any())).thenReturn(person);
        when(resultRepository.save(any(Result.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<PlayerResultUploadDTO> result = excelService.uploadResultsFromExcel(mockFile);

        // Assert
        assertThat(result).hasSize(1);
        verify(tournamentRepository).save(any(Tournament.class));
        verify(categoryRepository).save(any(Category.class));
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void shouldReturnEmptyListWhenExcelIsInvalid() {
        // Arrange
        MockMultipartFile badFile = new MockMultipartFile(
                "file", "bad.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        // Act
        List<PlayerResultUploadDTO> result = excelService.uploadResultsFromExcel(badFile);

        // Assert
        assertThat(result).isEmpty();
    }
}
