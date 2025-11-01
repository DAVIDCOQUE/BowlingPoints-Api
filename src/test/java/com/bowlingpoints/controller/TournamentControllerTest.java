package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.service.TournamentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Pruebas unitarias para TournamentController
 */
@WebMvcTest(controllers = TournamentController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TournamentService tournamentService;

    @Autowired
    private ObjectMapper objectMapper;

    private TournamentDTO tournamentDTO;

    @BeforeEach
    void setUp() {
        tournamentDTO = TournamentDTO.builder()
                .tournamentId(1)
                .name("Copa Colombia 2025")
                .startDate(LocalDate.of(2025, 3, 10))
                .endDate(LocalDate.of(2025, 3, 15))
                .status(true)
                .build();
    }

    // ===============================
    // GET /tournaments
    // ===============================
    @Test
    void getAll_ShouldReturnListOfTournaments() throws Exception {
        Mockito.when(tournamentService.getAll()).thenReturn(List.of(tournamentDTO));

        mockMvc.perform(get("/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Torneos cargados correctamente"))
                .andExpect(jsonPath("$.data[0].name").value("Copa Colombia 2025"));
    }

    // ===============================
    // GET /tournaments/{id}
    // ===============================
    @Test
    void getById_ShouldReturnTournament_WhenExists() throws Exception {
        Mockito.when(tournamentService.getById(1)).thenReturn(tournamentDTO);

        mockMvc.perform(get("/tournaments/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Torneo encontrado"))
                .andExpect(jsonPath("$.data.name").value("Copa Colombia 2025"));
    }

    @Test
    void getById_ShouldReturn404_WhenNotExists() throws Exception {
        Mockito.when(tournamentService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/tournaments/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Torneo no encontrado"));
    }

    // ===============================
    // GET /tournaments/ambit
    // ===============================
    @Test
    void getByAmbit_ShouldReturnListOfTournaments() throws Exception {
        Mockito.when(tournamentService.getTournamentsByAmbit(5, null))
                .thenReturn(List.of(tournamentDTO));

        mockMvc.perform(get("/tournaments/ambit")
                        .param("ambitId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Torneos por ámbito cargados correctamente"))
                .andExpect(jsonPath("$.data[0].name").value("Copa Colombia 2025"));
    }

    // ===============================
    // POST /tournaments
    // ===============================
    @Test
    void create_ShouldReturnCreatedTournament() throws Exception {
        Mockito.when(tournamentService.create(any(TournamentDTO.class)))
                .thenReturn(tournamentDTO);

        mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tournamentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Torneo creado correctamente"))
                .andExpect(jsonPath("$.data.name").value("Copa Colombia 2025"));
    }

    // ===============================
    // PUT /tournaments/{id}
    // ===============================
    @Test
    void update_ShouldReturnOk_WhenTournamentExists() throws Exception {
        Mockito.when(tournamentService.update(eq(1), any(TournamentDTO.class))).thenReturn(true);

        mockMvc.perform(put("/tournaments/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tournamentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Torneo actualizado correctamente"));
    }

    @Test
    void update_ShouldReturn404_WhenTournamentNotFound() throws Exception {
        Mockito.when(tournamentService.update(eq(99), any(TournamentDTO.class))).thenReturn(false);

        mockMvc.perform(put("/tournaments/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tournamentDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Torneo no encontrado para actualizar"));
    }

    // ===============================
    // DELETE /tournaments/{id}
    // ===============================
    @Test
    void delete_ShouldReturnOk_WhenDeletedSuccessfully() throws Exception {
        Mockito.when(tournamentService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/tournaments/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Torneo eliminado correctamente"));
    }

    @Test
    void delete_ShouldReturn404_WhenTournamentNotFound() throws Exception {
        Mockito.when(tournamentService.delete(99)).thenReturn(false);

        mockMvc.perform(delete("/tournaments/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Torneo no encontrado para eliminar"));
    }
}

