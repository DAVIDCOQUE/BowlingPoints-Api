package com.bowlingpoints.controller;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.AmbitService;
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

@WebMvcTest(controllers = AmbitController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class AmbitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmbitService ambitService; // ✅ Mockeamos el servicio real

    @Autowired
    private ObjectMapper objectMapper;

    private AmbitDTO ambitDTO;
    private ResponseGenericDTO<AmbitDTO> responseSingle;
    private ResponseGenericDTO<List<AmbitDTO>> responseList;
    private ResponseGenericDTO<Void> responseVoid;

    @BeforeEach
    void setUp() {
        ambitDTO = AmbitDTO.builder()
                .ambitId(1)
                .name("Nacional")
                .description("Torneos nacionales")
                .status(true)
                .build();

        responseSingle = new ResponseGenericDTO<>(true, "OK", ambitDTO);
        responseList = new ResponseGenericDTO<>(true, "OK", List.of(ambitDTO));
        responseVoid = new ResponseGenericDTO<>(true, "OK", null);
    }

    // ===============================
    // GET /ambits
    // ===============================
    @Test
    void getAll_ShouldReturnListOfAmbits() throws Exception {
        Mockito.when(ambitService.getAll()).thenReturn(responseList);

        mockMvc.perform(get("/ambits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Nacional"));
    }

    // ===============================
    // GET /ambits/{id}
    // ===============================
    @Test
    void getById_ShouldReturnAmbit() throws Exception {
        Mockito.when(ambitService.getById(1)).thenReturn(responseSingle);

        mockMvc.perform(get("/ambits/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nacional"));
    }

    // ===============================
    // POST /ambits
    // ===============================
    @Test
    void create_ShouldReturnCreatedAmbit() throws Exception {
        Mockito.when(ambitService.create(any(AmbitDTO.class))).thenReturn(responseSingle);

        mockMvc.perform(post("/ambits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ambitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nacional"));
    }

    // ===============================
    // PUT /ambits/{id}
    // ===============================
    @Test
    void update_ShouldReturnOkResponse() throws Exception {
        Mockito.when(ambitService.update(eq(1), any(AmbitDTO.class))).thenReturn(responseVoid);

        mockMvc.perform(put("/ambits/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ambitDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    // ===============================
    // DELETE /ambits/{id}
    // ===============================
    @Test
    void delete_ShouldReturnOkResponse() throws Exception {
        Mockito.when(ambitService.delete(1)).thenReturn(responseVoid);

        mockMvc.perform(delete("/ambits/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ===============================
    // GET /ambits/actives
    // ===============================
    @Test
    void getAllActives_ShouldReturnActiveAmbits() throws Exception {
        Mockito.when(ambitService.getAllActives()).thenReturn(responseList);

        mockMvc.perform(get("/ambits/actives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value(true));
    }

    // ===============================
    // GET /ambits/with-tournaments
    // ===============================
    @Test
    void getAmbitsWithTournaments_ShouldReturnList() throws Exception {
        Mockito.when(ambitService.getAmbitsWithTournaments()).thenReturn(responseList);

        mockMvc.perform(get("/ambits/with-tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Nacional"));
    }
}
