package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TeamDTO;
import com.bowlingpoints.service.TeamService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = TeamController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    @Autowired
    private ObjectMapper objectMapper;

    private TeamDTO teamDTO;

    @BeforeEach
    void setUp() {
        teamDTO = TeamDTO.builder()
                .teamId(1)
                .nameTeam("Los Strike Masters")
                .status(true)
                .build();
    }

    // ===============================
    // GET /teams
    // ===============================
   /* @Test
    void getAll_ShouldReturnListOfTeams() throws Exception {
        Mockito.when(teamService.getAll()).thenReturn(List.of(teamDTO));

        mockMvc.perform(get("/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipos cargados correctamente"))
                .andExpect(jsonPath("$.data[0].name").value("Los Strike Masters"));
    }

    */

    // ===============================
    // GET /teams/{id}
    // ===============================
   /* @Test
    void getById_ShouldReturnTeam_WhenExists() throws Exception {
        Mockito.when(teamService.getById(1)).thenReturn(teamDTO);

        mockMvc.perform(get("/teams/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo encontrado"))
                .andExpect(jsonPath("$.data.name").value("Los Strike Masters"));
    }

    */

    @Test
    void getById_ShouldReturnNotFound_WhenTeamDoesNotExist() throws Exception {
        Mockito.when(teamService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/teams/{id}", 99))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // POST /teams
    // ===============================
  /*  @Test
    void create_ShouldReturnCreatedTeam() throws Exception {
        Mockito.when(teamService.create(any(TeamDTO.class))).thenReturn(teamDTO);

        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo creado correctamente"))
                .andExpect(jsonPath("$.data.name").value("Los Strike Masters"));
    }*/

    // ===============================
    // PUT /teams/{id}
    // ===============================
    @Test
    void update_ShouldReturnOk_WhenTeamExists() throws Exception {
        Mockito.when(teamService.update(eq(1), any(TeamDTO.class))).thenReturn(true);

        mockMvc.perform(put("/teams/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo actualizado"));
    }

    @Test
    void update_ShouldReturnNotFound_WhenTeamNotExists() throws Exception {
        Mockito.when(teamService.update(eq(99), any(TeamDTO.class))).thenReturn(false);

        mockMvc.perform(put("/teams/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamDTO)))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // DELETE /teams/{id}
    // ===============================
    @Test
    void delete_ShouldReturnOk_WhenDeletedSuccessfully() throws Exception {
        Mockito.when(teamService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/teams/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo eliminado"));
    }

    @Test
    void delete_ShouldReturnNotFound_WhenTeamNotExists() throws Exception {
        Mockito.when(teamService.delete(99)).thenReturn(false);

        mockMvc.perform(delete("/teams/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
