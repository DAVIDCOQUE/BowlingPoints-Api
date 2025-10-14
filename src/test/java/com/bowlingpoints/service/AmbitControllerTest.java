package com.bowlingpoints.service;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AmbitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmbitService ambitService; // 🔹 sustituye al bean real, sin conflicto

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void resetMocks() {
        Mockito.reset(ambitService);
    }

    // -------------------------------
    // GET /ambits/all-ambit
    // -------------------------------
    @Test
    void testGetAllSuccess() throws Exception {
        List<AmbitDTO> ambits = new ArrayList<>();
        ambits.add(AmbitDTO.builder()
                        .ambitId(1)
                        .name("Ambito A")
                .build());
        ambits.add(AmbitDTO.builder()
                        .name("Ambito B")
                        .ambitId(2)
                .build());

        Mockito.when(ambitService.getAll())
                .thenReturn(new ResponseGenericDTO<>(true, "Consulta exitosa", ambits));

        mockMvc.perform(get("/ambits/all-ambit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Consulta exitosa"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Ambito A"));
    }

    @Test
    void testGetAllError() throws Exception {
        Mockito.when(ambitService.getAll())
                .thenThrow(new RuntimeException("Error al obtener ámbitos"));

        mockMvc.perform(get("/ambits/all-ambit"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message", containsString("Error al obtener ámbitos")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // -------------------------------
    // GET /ambits/{id}
    // -------------------------------
    @Test
    void testGetByIdSuccess() throws Exception {
        AmbitDTO ambit = AmbitDTO.builder()
                .name("Ambito 1")
                .ambitId(1)
                .build();

        Mockito.when(ambitService.getById(1))
                .thenReturn(new ResponseGenericDTO<>(true, "Consulta exitosa", ambit));

        mockMvc.perform(get("/ambits/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Consulta exitosa"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Ambito 1"));
    }

    @Test
    void testGetByIdError() throws Exception {
        Mockito.when(ambitService.getById(99))
                .thenThrow(new RuntimeException("No se encontró el ámbito con id 99"));

        mockMvc.perform(get("/ambits/{id}", 99))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message", containsString("No se encontró el ámbito con id 99")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // -------------------------------
    // POST /ambits/save-ambit
    // -------------------------------
    @Test
    void testCreateSuccess() throws Exception {
        AmbitDTO newAmbit = AmbitDTO.builder()
                .name("Nuevo Ambito")
                .ambitId(null)
                .build();
        AmbitDTO createdAmbit = AmbitDTO.builder()
                .ambitId(1)
                .name("Nuevo ambito")
                .build();

        Mockito.when(ambitService.create(Mockito.any(AmbitDTO.class)))
                .thenReturn(new ResponseGenericDTO<>(true, "Ámbito creado exitosamente", createdAmbit));

        mockMvc.perform(post("/ambits/save-ambit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAmbit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Ámbito creado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Nuevo Ámbito"));
    }

    @Test
    void testCreateError() throws Exception {
        AmbitDTO invalidAmbit = AmbitDTO.builder()
                .ambitId(null)
                .name("")
                .build();

        Mockito.when(ambitService.create(Mockito.any(AmbitDTO.class)))
                .thenThrow(new IllegalArgumentException("El nombre no puede estar vacío"));

        mockMvc.perform(post("/ambits/save-ambit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAmbit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message", containsString("El nombre no puede estar vacío")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // -------------------------------
    // PUT /ambits/{id}
    // -------------------------------
    @Test
    void testUpdateSuccess() throws Exception {
        AmbitDTO updatedAmbit = AmbitDTO.builder()
                .name("Ambito Actualizado")
                .ambitId(1).build();

        Mockito.when(ambitService.update(Mockito.eq(1), Mockito.any(AmbitDTO.class)))
                .thenReturn(new ResponseGenericDTO<>(true, "Ámbito actualizado correctamente", null));

        mockMvc.perform(put("/ambits/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAmbit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Ámbito actualizado correctamente"));
    }

    @Test
    void testUpdateError() throws Exception {
        AmbitDTO ambit = AmbitDTO.builder()
                .ambitId(99)
                .name("Inexistente")
                .build();

        Mockito.when(ambitService.update(Mockito.eq(99), Mockito.any(AmbitDTO.class)))
                .thenThrow(new RuntimeException("No se encontró el ámbito con id 99"));

        mockMvc.perform(put("/ambits/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ambit)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message", containsString("No se encontró el ámbito con id 99")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // -------------------------------
    // DELETE /ambits/{id}
    // -------------------------------
    @Test
    void testDeleteSuccess() throws Exception {
        Mockito.when(ambitService.delete(1))
                .thenReturn(new ResponseGenericDTO<>(true, "Ámbito eliminado correctamente", null));

        mockMvc.perform(delete("/ambits/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Ámbito eliminado correctamente"));
    }

    @Test
    void testDeleteError() throws Exception {
        Mockito.when(ambitService.delete(99))
                .thenThrow(new RuntimeException("No se encontró el ámbito con id 99"));

        mockMvc.perform(delete("/ambits/{id}", 99))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message", containsString("No se encontró el ámbito con id 99")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}
