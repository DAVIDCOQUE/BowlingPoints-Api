package com.bowlingpoints.controller;

import com.bowlingpoints.dto.TournamentRegistrationDTO;
import com.bowlingpoints.service.TournamentRegistrationService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Pruebas unitarias para TournamentRegistrationController
 */
@WebMvcTest(controllers = TournamentRegistrationController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false) // ✅ Evita cargar filtros JWT o seguridad
class TournamentRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TournamentRegistrationService registrationService;

    @Autowired
    private ObjectMapper objectMapper;

    private TournamentRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        registrationDTO = TournamentRegistrationDTO.builder()
                .registrationId(1)
                .tournamentId(10)
                .personId(20)
                .categoryId(5)
                .modalityId(3)
                .branchId(2)
                .teamId(7)
                .status(true)
                .build();
    }

    // ===============================
    // POST /registrations
    // ===============================
    @Test
    void create_ShouldReturnCreatedRegistration() throws Exception {
        Mockito.when(registrationService.create(any(TournamentRegistrationDTO.class)))
                .thenReturn(registrationDTO);

        mockMvc.perform(post("/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentId").value(10))
                .andExpect(jsonPath("$.personId").value(20));
    }

    // ===============================
    // GET /registrations
    // ===============================
    @Test
    void getAll_ShouldReturnListOfRegistrations() throws Exception {
        Mockito.when(registrationService.getAll()).thenReturn(List.of(registrationDTO));

        mockMvc.perform(get("/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationId").value(1))
                .andExpect(jsonPath("$[0].tournamentId").value(10));
    }

    // ===============================
    // GET /registrations/tournament/{tournamentId}
    // ===============================
    @Test
    void getByTournament_ShouldReturnRegistrationsByTournament() throws Exception {
        Mockito.when(registrationService.getByTournament(10))
                .thenReturn(List.of(registrationDTO));

        mockMvc.perform(get("/registrations/tournament/{tournamentId}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tournamentId").value(10));
    }

    // ===============================
    // GET /registrations/person/{personId}
    // ===============================
    @Test
    void getByPerson_ShouldReturnRegistrationsByPerson() throws Exception {
        Mockito.when(registrationService.getByPerson(20))
                .thenReturn(List.of(registrationDTO));

        mockMvc.perform(get("/registrations/person/{personId}", 20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personId").value(20));
    }

    // ===============================
    // GET /registrations/{id}
    // ===============================
    @Test
    void getById_ShouldReturnRegistration() throws Exception {
        Mockito.when(registrationService.getById(1)).thenReturn(registrationDTO);

        mockMvc.perform(get("/registrations/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationId").value(1));
    }

    // ===============================
    // PUT /registrations/{id}
    // ===============================
    @Test
    void update_ShouldReturnUpdatedRegistration() throws Exception {
        Mockito.when(registrationService.update(eq(1), any(TournamentRegistrationDTO.class)))
                .thenReturn(registrationDTO);

        mockMvc.perform(put("/registrations/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentId").value(10));
    }

    // ===============================
    // DELETE /registrations/{id}
    // ===============================
    @Test
    void delete_ShouldReturnOk_WhenDeletedSuccessfully() throws Exception {
        Mockito.when(registrationService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/registrations/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("Registro eliminado correctamente"));
    }

    @Test
    void delete_ShouldReturnNotFound_WhenDeleteFails() throws Exception {
        Mockito.when(registrationService.delete(99)).thenReturn(false);

        mockMvc.perform(delete("/registrations/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
