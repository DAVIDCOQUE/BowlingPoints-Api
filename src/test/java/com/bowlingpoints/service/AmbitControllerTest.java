package com.bowlingpoints.service;
import com.bowlingpoints.controller.AmbitController;
import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.AmbitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AmbitController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {AmbitController.class}) // ✅ evita que cargue todo el contexto de SpringBootApplication
class AmbitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmbitService ambitService;

    @Autowired
    private ObjectMapper objectMapper;

    private AmbitDTO ambitDTO;
    private ResponseGenericDTO<AmbitDTO> responseSingle;
    private ResponseGenericDTO<List<AmbitDTO>> responseList;
    private ResponseGenericDTO<Void> responseVoid;

    @Test
    void getAll_ShouldReturnListOfAmbits() throws Exception {
        Mockito.when(ambitService.getAll()).thenReturn(responseList);

        mockMvc.perform(get("/ambits/all-ambit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Test Ambit"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void getById_ShouldReturnAmbitById() throws Exception {
        Mockito.when(ambitService.getById(1)).thenReturn(responseSingle);

        mockMvc.perform(get("/ambits/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Ambit"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void create_ShouldReturnCreatedAmbit() throws Exception {
        Mockito.when(ambitService.create(any(AmbitDTO.class))).thenReturn(responseSingle);

        mockMvc.perform(post("/ambits/save-ambit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ambitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Ambit"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void update_ShouldReturnSuccessResponse() throws Exception {
        Mockito.when(ambitService.update(eq(1), any(AmbitDTO.class))).thenReturn(responseVoid);

        mockMvc.perform(put("/ambits/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ambitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void delete_ShouldReturnSuccessResponse() throws Exception {
        Mockito.when(ambitService.delete(1)).thenReturn(responseVoid);

        mockMvc.perform(delete("/ambits/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted"))
                .andExpect(jsonPath("$.status").value(true));
    }
}



