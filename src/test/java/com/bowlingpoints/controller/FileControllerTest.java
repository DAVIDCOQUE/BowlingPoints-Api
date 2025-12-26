package com.bowlingpoints.controller;

import com.bowlingpoints.dto.PersonImportResponse;
import com.bowlingpoints.service.PersonImportService;
import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.service.TeamPersonImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonImportService personImportService;

    @MockBean
    private TeamPersonImportService teamPersonimportService;

        @MockBean
        private JwtService jwtService;

    @Test
    void importPersons_Success() throws Exception {
        String csv = "document;names;surnames;email;gender;birthDate;phone\n" +
                "123;John;Doe;john@example.com;M;1/5/1990;555\n";

        MockMultipartFile file = new MockMultipartFile("file", "persons.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        PersonImportResponse resp = PersonImportResponse.builder()
                .successCount(1)
                .errorCount(0)
                .totalProcessed(1)
                .errors(Collections.emptyList())
                .build();

        when(personImportService.importPersonFile(any())).thenReturn(resp);

        mockMvc.perform(multipart("/files/persons")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(0));
    }

    @Test
    void importPersons_EmptyFile_BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "persons.csv", "text/csv",
                new byte[0]);

        mockMvc.perform(multipart("/files/persons")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importTeamPerson_Success() throws Exception {
        String csv = "documentNumber,teamName\n123,Team A\n";
        MockMultipartFile file = new MockMultipartFile("file", "team-person.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        var importResult = new TeamPersonImportService.ImportResult(1, 0, Collections.emptyList());
        when(teamPersonimportService.importCsv(any(), eq(42), eq(true))).thenReturn(importResult);

        mockMvc.perform(multipart("/files/team-person")
                        .file(file)
                        .param("userId", "42")
                        .param("skipHeader", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.skipped").value(0));
    }

    @Test
    void importTeamPerson_EmptyFile_BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "team-person.csv", "text/csv",
                new byte[0]);

        mockMvc.perform(multipart("/files/team-person")
                        .file(file)
                        .param("userId", "1")
                        .param("skipHeader", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}
