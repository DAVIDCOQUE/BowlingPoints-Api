package com.bowlingpoints.controller;

import com.bowlingpoints.dto.PlayerResultUploadDTO;
import com.bowlingpoints.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {FileController.class})  // ✅ fuerza contexto mínimo
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    private MockMultipartFile mockFile;
    private List<PlayerResultUploadDTO> mockResults;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy-data".getBytes()
        );

        PlayerResultUploadDTO result = new PlayerResultUploadDTO();
        // Si PlayerResultUploadDTO tiene campos, puedes inicializarlos aquí
        mockResults = List.of(result);
    }

    @Test
    void shouldUploadExcelFileSuccessfully() throws Exception {
        // Arrange
        when(fileService.uploadResultsFromExcel(any())).thenReturn(mockResults);

        // Act & Assert
        mockMvc.perform(multipart("/file/process/upload-results")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))); // Espera lista con un elemento
    }
}
