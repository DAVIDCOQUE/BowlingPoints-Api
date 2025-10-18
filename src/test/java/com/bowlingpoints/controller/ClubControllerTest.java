package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.repository.ClubRepository;
import com.bowlingpoints.service.ClubPersonService;
import com.bowlingpoints.service.ClubsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ClubController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClubsService clubsService;

    @MockBean
    private ClubPersonService clubPersonService;

    @MockBean
    private ClubRepository clubRepository;

    // 👇 Mocks necesarios para evitar errores por seguridad
    @MockBean
    private com.bowlingpoints.config.jwt.JwtService jwtService;

    @MockBean
    private com.bowlingpoints.config.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private ClubsDTO clubsDTO;
    private ClubDetailsDTO clubDetailsDTO;
    private ClubPersonDTO memberDTO;

    @BeforeEach
    void setUp() {
        memberDTO = ClubPersonDTO.builder()
                .personId(1)
                .photoUrl("https://photo.url")
                .fullName("John Doe")
                .email("john@example.com")
                .roleInClub("Presidente")
                .joinedAt(LocalDateTime.now())
                .build();

        clubsDTO = ClubsDTO.builder()
                .clubId(1)
                .name("Test Club")
                .foundationDate(LocalDate.of(2020, 1, 1))
                .city("Medellín")
                .description("Un club de prueba")
                .imageUrl("https://image.url")
                .status(true)
                .members(List.of())
                .build();

        clubDetailsDTO = ClubDetailsDTO.builder()
                .clubId(1)
                .name("Test Club")
                .foundationDate(LocalDate.of(2020, 1, 1))
                .city("Medellín")
                .description("Un club de prueba")
                .imageUrl("https://image.url")
                .status(true)
                .members(List.of(memberDTO))
                .build();
    }

    @Test
    void getAllClubsWithMembers_ShouldReturnList() throws Exception {
        when(clubsService.getAllClubsWithMembers()).thenReturn(List.of(clubDetailsDTO));

        mockMvc.perform(get("/clubs/with-members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clubId").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Club"));
    }

    @Test
    void getClubDetails_ShouldReturnDetails() throws Exception {
        when(clubsService.getClubDetails(1)).thenReturn(clubDetailsDTO);

        mockMvc.perform(get("/clubs/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(1))
                .andExpect(jsonPath("$.name").value("Test Club"));
    }

    @Test
    void createClubWithMembers_ShouldReturnCreatedClub() throws Exception {
        Clubs savedClub = new Clubs();
        savedClub.setClubId(1);

        when(clubsService.createClubWithMembers(any(ClubsDTO.class))).thenReturn(savedClub);
        when(clubsService.getClubDetails(1)).thenReturn(clubDetailsDTO);

        mockMvc.perform(post("/clubs/create-with-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(1));
    }

    @Test
    void updateClub_Success_ShouldReturnOk() throws Exception {
        mockMvc.perform(put("/clubs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Club actualizado correctamente"));
    }

    @Test
    void updateClub_Failure_ShouldReturnNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Club no encontrado"))
                .when(clubsService).updateClubWithMembers(any(Integer.class), any(ClubsDTO.class));

        mockMvc.perform(put("/clubs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubsDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Club no encontrado"));
    }

    @Test
    void deleteClub_ShouldReturnOk() throws Exception {
        when(clubsService.deleteClub(1)).thenReturn(true);

        mockMvc.perform(delete("/clubs/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteClub_NotFound_ShouldReturn404() throws Exception {
        when(clubsService.deleteClub(99)).thenReturn(false);

        mockMvc.perform(delete("/clubs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getClubMembers_ShouldReturnMemberList() throws Exception {
        ResponseGenericDTO<List<ClubPersonDTO>> response = new ResponseGenericDTO<>();
        response.setSuccess(true);
        response.setMessage("Miembros cargados");
        response.setData(List.of(memberDTO));

        when(clubPersonService.getMembersByClubId(1)).thenReturn(response);

        mockMvc.perform(get("/clubs/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].personId").value(1))
                .andExpect(jsonPath("$.message").value("Miembros cargados"));
    }
}
