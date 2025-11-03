package com.bowlingpoints.controller;

import com.bowlingpoints.dto.TeamDTO;
import com.bowlingpoints.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {TeamController.class})
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    // 🔹 GET /teams
    @Test
    void getAll_ShouldReturnListOfTeams() throws Exception {
        TeamDTO t1 = new TeamDTO(1, "Team A", "111111111", true, List.of(1, 2));
        TeamDTO t2 = new TeamDTO(2, "Team B", "222222222", true, List.of(3, 4));

        when(teamService.getAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/teams").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipos cargados correctamente"))
                .andExpect(jsonPath("$.data[0].nameTeam").value("Team A"))
                .andExpect(jsonPath("$.data[1].nameTeam").value("Team B"));

        verify(teamService, times(1)).getAll();
    }

    // 🔹 GET /teams/{id}
    @Test
    void getById_ShouldReturnTeam_WhenExists() throws Exception {
        TeamDTO team = new TeamDTO(1, "Team X", "999999999", true, List.of(10, 11));
        when(teamService.getById(1)).thenReturn(team);

        mockMvc.perform(get("/teams/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo encontrado"))
                .andExpect(jsonPath("$.data.nameTeam").value("Team X"));
    }

    @Test
    void getById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(teamService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/teams/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // 🔹 POST /teams
    @Test
    void create_ShouldReturnCreatedTeam() throws Exception {
        TeamDTO input = new TeamDTO(null, "Team New", "123456789", true, List.of(5, 6));
        TeamDTO created = new TeamDTO(5, "Team New", "123456789", true, List.of(5, 6));

        when(teamService.create(any(TeamDTO.class))).thenReturn(created);

        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameTeam\":\"Team New\",\"phone\":\"123456789\",\"status\":true,\"personIds\":[5,6]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo creado correctamente"))
                .andExpect(jsonPath("$.data.nameTeam").value("Team New"));
    }

    // 🔹 PUT /teams/{id}
    @Test
    void update_ShouldReturnOk_WhenSuccessful() throws Exception {
        when(teamService.update(eq(1), any(TeamDTO.class))).thenReturn(true);

        mockMvc.perform(put("/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameTeam\":\"Updated Team\",\"phone\":\"987654321\",\"status\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo actualizado"));
    }

    @Test
    void update_ShouldReturnNotFound_WhenTeamDoesNotExist() throws Exception {
        when(teamService.update(eq(99), any(TeamDTO.class))).thenReturn(false);

        mockMvc.perform(put("/teams/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameTeam\":\"NonExistent\"}"))
                .andExpect(status().isNotFound());
    }

    // 🔹 DELETE /teams/{id}
    @Test
    void delete_ShouldReturnOk_WhenDeleted() throws Exception {
        when(teamService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Equipo eliminado"));
    }

    @Test
    void delete_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(teamService.delete(99)).thenReturn(false);

        mockMvc.perform(delete("/teams/99"))
                .andExpect(status().isNotFound());
    }
}
