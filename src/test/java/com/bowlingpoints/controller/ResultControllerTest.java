package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.service.ResultService;
import com.bowlingpoints.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    @Mock
    private ResultService resultService;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private ResultController resultController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ResultDTO sampleResult;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(resultController).build();
        objectMapper = new ObjectMapper();

        sampleResult = ResultDTO.builder()
                .resultId(1)
                .personId(1)
                .personName("John Doe")
                .score(200)
                .build();
    }

    // -------------------------------------------------
    // CRUD ENDPOINTS
    // -------------------------------------------------

    @Test
    void getAll_ShouldReturnOkWithResults() throws Exception {
        when(resultService.getAll()).thenReturn(List.of(sampleResult));

        mockMvc.perform(get("/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].personName").value("John Doe"));
    }

    @Test
    void getById_ShouldReturnOk_WhenFound() throws Exception {
        when(resultService.getById(1)).thenReturn(sampleResult);

        mockMvc.perform(get("/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.personName").value("John Doe"));
    }

    @Test
    void getById_ShouldReturn404_WhenNotFound() throws Exception {
        when(resultService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/results/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_ShouldReturnOk_WhenCreated() throws Exception {
        when(resultService.create(any(ResultDTO.class))).thenReturn(sampleResult);

        mockMvc.perform(post("/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.personName").value("John Doe"));
    }

    @Test
    void update_ShouldReturnOk_WhenUpdated() throws Exception {
        when(resultService.update(eq(1), any(ResultDTO.class))).thenReturn(true);

        mockMvc.perform(put("/results/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleResult)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resultado actualizado correctamente"));
    }

    @Test
    void update_ShouldReturn404_WhenNotFound() throws Exception {
        when(resultService.update(eq(1), any(ResultDTO.class))).thenReturn(false);

        mockMvc.perform(put("/results/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleResult)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void delete_ShouldReturnOk_WhenDeleted() throws Exception {
        when(resultService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_ShouldReturn404_WhenNotFound() throws Exception {
        when(resultService.delete(1)).thenReturn(false);

        mockMvc.perform(delete("/results/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // -------------------------------------------------
    // FILTER & ADVANCED ENDPOINTS
    // -------------------------------------------------

    @Test
    void getFilteredResults_ShouldReturnFilteredList() throws Exception {
        when(resultService.getResultsByTournamentFiltered(1, null, null))
                .thenReturn(List.of(sampleResult));

        mockMvc.perform(get("/results/filter")
                        .param("tournamentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(200));
    }

    @Test
    void getTournamentResultsTable_ShouldReturnOk() throws Exception {
        TournamentResultsResponseDTO dto = TournamentResultsResponseDTO.builder().build();
        when(resultService.getTournamentResultsTable(eq(1), eq(2), any())).thenReturn(dto);

        mockMvc.perform(get("/results/tournament-table")
                        .param("tournamentId", "1")
                        .param("modalityId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getResultsByModality_ShouldReturnOk() throws Exception {
        TournamentResultsResponseDTO dto = TournamentResultsResponseDTO.builder().build();
        when(resultService.getResultsByModality(eq(1), any(), any())).thenReturn(dto);

        mockMvc.perform(get("/results/by-modality")
                        .param("tournamentId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllPlayerRanking_ShouldReturnOk() throws Exception {
        DashboardPlayerDTO player = DashboardPlayerDTO.builder().fullName("John").averageScore(200.0).build();
        when(resultService.getAllPlayersByAvgScore()).thenReturn(List.of(player));

        mockMvc.perform(get("/results/all-player-ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fullName").value("John"));
    }

    @Test
    void getTournamentsByAmbit_ShouldReturnOk() throws Exception {
        TournamentDTO t = TournamentDTO.builder().tournamentId(1).name("Test").build();
        when(tournamentService.getTournamentsByAmbit(any(), any())).thenReturn(List.of(t));

        mockMvc.perform(get("/results/by-ambit")
                        .param("ambitName", "Nacional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Test"));
    }
}
