package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtAuthenticationFilter;
import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.ModalityService;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModalityController.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class ModalityControllerTest {

    @MockBean
    private ModalityService modalityService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ModalityDTO testModality;

    @BeforeEach
    void setUp() {
        testModality = new ModalityDTO();
        testModality.setModalityId(1);
        testModality.setName("Test Modality");
        testModality.setDescription("Testing modality");
        testModality.setStatus(true);
    }

    @Test
    void getAll_ShouldReturnModalities() throws Exception {
        when(modalityService.getAll()).thenReturn(List.of(testModality));

        mockMvc.perform(get("/modalities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Modalidades cargadas correctamente"))
                .andExpect(jsonPath("$.data[0].modalityId").value(1));
    }

    @Test
    void getById_ShouldReturnModality_WhenExists() throws Exception {
        when(modalityService.getById(1)).thenReturn(testModality);

        mockMvc.perform(get("/modalities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Modalidad encontrada"))
                .andExpect(jsonPath("$.data.modalityId").value(1));
    }

    @Test
    void getById_ShouldReturnNotFound_WhenModalityDoesNotExist() throws Exception {
        when(modalityService.getById(1)).thenReturn(null);

        mockMvc.perform(get("/modalities/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturnCreatedModality() throws Exception {
        when(modalityService.create(testModality)).thenReturn(testModality);

        mockMvc.perform(post("/modalities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testModality)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Modalidad creada correctamente"))
                .andExpect(jsonPath("$.data.modalityId").value(1));
    }

    @Test
    void update_ShouldReturnSuccess_WhenModalityExists() throws Exception {
        when(modalityService.update(1, testModality)).thenReturn(true);

        mockMvc.perform(put("/modalities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testModality)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Modalidad actualizada"));
    }

    @Test
    void update_ShouldReturnNotFound_WhenModalityDoesNotExist() throws Exception {
        when(modalityService.update(1, testModality)).thenReturn(false);

        mockMvc.perform(put("/modalities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testModality)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldReturnSuccess_WhenModalityExists() throws Exception {
        when(modalityService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/modalities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Modalidad eliminada"));
    }

    @Test
    void delete_ShouldReturnNotFound_WhenModalityDoesNotExist() throws Exception {
        when(modalityService.delete(1)).thenReturn(false);

        mockMvc.perform(delete("/modalities/1"))
                .andExpect(status().isNotFound());
    }
}
