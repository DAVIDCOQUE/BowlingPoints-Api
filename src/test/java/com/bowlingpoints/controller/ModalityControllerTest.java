package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.ModalityService;
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

/**
 * 🧪 Pruebas unitarias para ModalityController
 */
@WebMvcTest(controllers = ModalityController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false) // ✅ evita cargar filtros JWT o seguridad
class ModalityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModalityService modalityService;

    @Autowired
    private ObjectMapper objectMapper;

    private ModalityDTO modalityDTO;
    private ResponseGenericDTO<ModalityDTO> responseSingle;
    private ResponseGenericDTO<List<ModalityDTO>> responseList;
    private ResponseGenericDTO<Void> responseVoid;

    @BeforeEach
    void setUp() {
        modalityDTO = ModalityDTO.builder()
                .modalityId(1)
                .name("Individual")
                .description("Competencia individual")
                .status(true)
                .build();

        responseSingle = new ResponseGenericDTO<>(true, "OK", modalityDTO);
        responseList = new ResponseGenericDTO<>(true, "OK", List.of(modalityDTO));
        responseVoid = new ResponseGenericDTO<>(true, "OK", null);
    }

    // ===============================
    // GET /modalities
    // ===============================
    @Test
    void getAll_ShouldReturnListOfModalities() throws Exception {
        Mockito.when(modalityService.getAll()).thenReturn(responseList);

        mockMvc.perform(get("/modalities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Individual"));
    }

    // ===============================
    // GET /modalities/actives
    // ===============================
    @Test
    void getAllActives_ShouldReturnActiveModalities() throws Exception {
        Mockito.when(modalityService.getAllActives()).thenReturn(responseList);

        mockMvc.perform(get("/modalities/actives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value(true));
    }

    // ===============================
    // GET /modalities/{id}
    // ===============================
    @Test
    void getById_ShouldReturnModality() throws Exception {
        Mockito.when(modalityService.getById(1)).thenReturn(responseSingle);

        mockMvc.perform(get("/modalities/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Individual"));
    }

    // ===============================
    // POST /modalities
    // ===============================
    @Test
    void create_ShouldReturnCreatedModality() throws Exception {
        Mockito.when(modalityService.create(any(ModalityDTO.class))).thenReturn(responseSingle);

        mockMvc.perform(post("/modalities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modalityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Individual"));
    }

    // ===============================
    // PUT /modalities/{id}
    // ===============================
    @Test
    void update_ShouldReturnOkResponse() throws Exception {
        Mockito.when(modalityService.update(eq(1), any(ModalityDTO.class))).thenReturn(responseVoid);

        mockMvc.perform(put("/modalities/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modalityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    // ===============================
    // DELETE /modalities/{id}
    // ===============================
    @Test
    void delete_ShouldReturnOkResponse() throws Exception {
        Mockito.when(modalityService.delete(1)).thenReturn(responseVoid);

        mockMvc.perform(delete("/modalities/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"));
    }
}
