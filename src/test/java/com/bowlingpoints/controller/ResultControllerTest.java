package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.service.ResultService;
import com.bowlingpoints.service.TournamentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ResultController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class ResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResultService resultService;

    @MockBean
    private TournamentService tournamentService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultDTO resultDTO;
    private PlayerResultSummaryDTO summaryDTO;
    private PlayerResultTableDTO tableDTO;
    private DashboardPlayerDTO playerDTO;
    private TournamentDTO tournamentDTO;

    @BeforeEach
    void setUp() {
        resultDTO = ResultDTO.builder()
                .resultId(1)
                .tournamentId(10)
                .personId(20)
                .score(180)
                .lineNumber(1)
                .build();

        summaryDTO = PlayerResultSummaryDTO.builder()
                .playerName("John Doe")
                .promedioGlobal(190.5)
                .build();

        tableDTO = PlayerResultTableDTO.builder()
                .playerName("John Doe")
                .total(950)
                .promedio(190.0)
                .build();

        playerDTO = DashboardPlayerDTO.builder()
                .fullName("John Doe")
                .averageScore(195.2)
                .build();

        tournamentDTO = TournamentDTO.builder()
                .tournamentId(5)
                .name("Copa Colombia")
                .build();
    }

    // --------------------------------------------------
    // CRUD de Resultados
    // --------------------------------------------------

    @Test
    void getAll_ShouldReturnListOfResults() throws Exception {
        Mockito.when(resultService.getAll()).thenReturn(List.of(resultDTO));

        mockMvc.perform(get("/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tournamentId").value(10))
                .andExpect(jsonPath("$.message").value("Resultados cargados correctamente"));
    }

    @Test
    void getById_ShouldReturnResult_WhenExists() throws Exception {
        Mockito.when(resultService.getById(1)).thenReturn(resultDTO);

        mockMvc.perform(get("/results/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resultId").value(1))
                .andExpect(jsonPath("$.message").value("Resultado encontrado"));
    }

    @Test
    void getById_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(resultService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/results/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Resultado no encontrado"));
    }

    @Test
    void create_ShouldReturnCreatedResult() throws Exception {
        Mockito.when(resultService.create(any(ResultDTO.class))).thenReturn(resultDTO);

        mockMvc.perform(post("/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resultado creado correctamente"))
                .andExpect(jsonPath("$.data.resultId").value(1));
    }

    @Test
    void update_ShouldReturnOk_WhenUpdated() throws Exception {
        Mockito.when(resultService.update(eq(1), any(ResultDTO.class))).thenReturn(true);

        mockMvc.perform(put("/results/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resultado actualizado correctamente"));
    }

    @Test
    void update_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(resultService.update(eq(99), any(ResultDTO.class))).thenReturn(false);

        mockMvc.perform(put("/results/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resultado no encontrado"));
    }

    @Test
    void delete_ShouldReturnOk_WhenDeleted() throws Exception {
        Mockito.when(resultService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/results/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resultado eliminado correctamente"));
    }

    @Test
    void delete_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(resultService.delete(99)).thenReturn(false);

        mockMvc.perform(delete("/results/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resultado no encontrado"));
    }

    // --------------------------------------------------
    // Estadísticas y Agrupaciones
    // --------------------------------------------------

    @Test
    void getResultsByGender_ShouldReturnGroupedResults() throws Exception {
        Mockito.when(resultService.getTournamentResultsByGender(10))
                .thenReturn(Map.of("Masculino", List.of(summaryDTO)));

        mockMvc.perform(get("/results/by-gender").param("tournamentId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resultados agrupados por género"))
                .andExpect(jsonPath("$.data.Masculino[0].playerName").value("John Doe"));
    }

    @Test
    void getResultsTable_ShouldReturnResultsTable() throws Exception {
        Mockito.when(resultService.getPlayerResultsForTable(10, 3))
                .thenReturn(List.of(tableDTO));

        mockMvc.perform(get("/results/table")
                        .param("tournamentId", "10")
                        .param("modalityId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tabla de resultados"))
                .andExpect(jsonPath("$.data[0].playerName").value("John Doe"));
    }

    /*@Test
    void getAllPlayerRanking_ShouldReturnRankingList() throws Exception {
        Mockito.when(resultService.getAllPlayersByAvgScore())
                .thenReturn(List.of(playerDTO));

        mockMvc.perform(get("/results/all-player-ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ranking cargado correctamente"))
                .andExpect(jsonPath("$.data[0].playerName").value("John Doe"));
    }

     */

    // --------------------------------------------------
    // Torneos desde Resultados
    // --------------------------------------------------

    @Test
    void getTournamentsByAmbit_ShouldReturnListOfTournaments() throws Exception {
        Mockito.when(tournamentService.getTournamentsByAmbit(1, null))
                .thenReturn(List.of(tournamentDTO));

        mockMvc.perform(get("/results/by-ambit").param("ambitId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Torneos cargados correctamente"))
                .andExpect(jsonPath("$.data[0].name").value("Copa Colombia"));
    }
}
