package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ClubDTO;
import com.bowlingpoints.dto.ClubPersonDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.ClubPersonService;
import com.bowlingpoints.service.ClubService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ClubController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClubService clubService;

    @MockBean
    private ClubPersonService clubPersonService;

    @Autowired
    private ObjectMapper objectMapper;

    private ClubDTO clubDTO;
    private ClubPersonDTO clubPersonDTO;

    @BeforeEach
    void setUp() {
        clubDTO = ClubDTO.builder()
                .clubId(1)
                .name("Club Strike Force")
                .foundationDate(LocalDate.of(2020, 5, 10))
                .status(true)
                .build();

        clubPersonDTO = ClubPersonDTO.builder()
                .clubPersonId(100)
                .clubId(1)
                .personId(200)
                .roleInClub("Jugador")
                .build();
    }

    // ===============================
    // CLUBES
    // ===============================

    @Test
    void getAllClubs_ShouldReturnList() throws Exception {
        Mockito.when(clubService.getAllClubsNotDeleted()).thenReturn(List.of(clubDTO));

        mockMvc.perform(get("/clubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Clubes obtenidos correctamente"))
                .andExpect(jsonPath("$.data[0].name").value("Club Strike Force"));
    }

    @Test
    void getActiveClubs_ShouldReturnList() throws Exception {
        Mockito.when(clubService.getAllActiveClubs()).thenReturn(List.of(clubDTO));

        mockMvc.perform(get("/clubs/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Clubes activos obtenidos correctamente"))
                .andExpect(jsonPath("$.data[0].status").value(true));
    }

    @Test
    void getClubById_ShouldReturnClub_WhenExists() throws Exception {
        Mockito.when(clubService.getClubById(1)).thenReturn(clubDTO);

        mockMvc.perform(get("/clubs/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Club obtenido correctamente"))
                .andExpect(jsonPath("$.data.name").value("Club Strike Force"));
    }

    @Test
    void getClubById_ShouldReturn404_WhenNotExists() throws Exception {
        Mockito.when(clubService.getClubById(99)).thenReturn(null);

        mockMvc.perform(get("/clubs/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void createClub_ShouldReturnOkResponse() throws Exception {
        Mockito.doNothing().when(clubService).createClub(any(ClubDTO.class));

        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Club creado correctamente"));
    }

    @Test
    void updateClub_ShouldReturnOk_WhenUpdated() throws Exception {
        Mockito.when(clubService.updateClub(eq(1), any(ClubDTO.class))).thenReturn(true);

        mockMvc.perform(put("/clubs/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Club actualizado correctamente"));
    }

    @Test
    void updateClub_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(clubService.updateClub(eq(99), any(ClubDTO.class))).thenReturn(false);

        mockMvc.perform(put("/clubs/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClub_ShouldReturnOk_WhenDeleted() throws Exception {
        Mockito.when(clubService.deleteClub(1)).thenReturn(true);

        mockMvc.perform(delete("/clubs/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Club eliminado correctamente"));
    }

    @Test
    void deleteClub_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(clubService.deleteClub(99)).thenReturn(false);

        mockMvc.perform(delete("/clubs/{id}", 99))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // MIEMBROS
    // ===============================

    @Test
    void addMemberToClub_ShouldReturnOk_WhenSuccess() throws Exception {
        Mockito.when(clubPersonService.addMemberToClub(any(ClubPersonDTO.class))).thenReturn(true);

        mockMvc.perform(post("/clubs/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubPersonDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Miembro agregado correctamente al club"));
    }

    @Test
    void addMemberToClub_ShouldReturnBadRequest_WhenFails() throws Exception {
        Mockito.when(clubPersonService.addMemberToClub(any(ClubPersonDTO.class))).thenReturn(false);

        mockMvc.perform(post("/clubs/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubPersonDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No se pudo agregar el miembro. Verifica que el club y la persona existan."));
    }

    @Test
    void removeMemberFromClub_ShouldReturnOk_WhenRemoved() throws Exception {
        Mockito.when(clubPersonService.removeMember(100)).thenReturn(true);

        mockMvc.perform(delete("/clubs/members/{clubPersonId}", 100))
                .andExpect(status().isOk())
                .andExpect(content().string("Miembro eliminado correctamente del club"));
    }

    @Test
    void removeMemberFromClub_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(clubPersonService.removeMember(999)).thenReturn(false);

        mockMvc.perform(delete("/clubs/members/{clubPersonId}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMemberRole_ShouldReturnOk_WhenUpdated() throws Exception {
        Mockito.when(clubPersonService.updateMemberRole(100, "Capitán")).thenReturn(true);

        mockMvc.perform(patch("/clubs/members/{clubPersonId}/role", 100)
                        .param("newRole", "Capitán"))
                .andExpect(status().isOk())
                .andExpect(content().string("Rol del miembro actualizado correctamente"));
    }

    @Test
    void updateMemberRole_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(clubPersonService.updateMemberRole(999, "Capitán")).thenReturn(false);

        mockMvc.perform(patch("/clubs/members/{clubPersonId}/role", 999)
                        .param("newRole", "Capitán"))
                .andExpect(status().isNotFound());
    }
}
