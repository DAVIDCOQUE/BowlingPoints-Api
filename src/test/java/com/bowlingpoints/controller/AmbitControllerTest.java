package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtAuthenticationFilter;
import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.AmbitService;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(AmbitController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AmbitControllerTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmbitService ambitService;

    @Autowired
    private ObjectMapper objectMapper;

    private AmbitDTO testAmbit;

    @BeforeEach
    void setUp() {
        testAmbit = new AmbitDTO();
        testAmbit.setAmbitId(1);
        testAmbit.setName("Test Ambit");
        testAmbit.setDescription("Test Description");
        testAmbit.setStatus(true);
        testAmbit.setImageUrl("test.png");
    }

    @Test
    void getAll_ShouldReturnListOfAmbits() throws Exception {
        ResponseGenericDTO<List<AmbitDTO>> response = new ResponseGenericDTO<>(true, "Success", List.of(testAmbit));
        when(ambitService.getAll()).thenReturn(response);

        mockMvc.perform(get("/ambits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].ambitId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Test Ambit"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getById_ShouldReturnAmbit() throws Exception {
        ResponseGenericDTO<AmbitDTO> response = new ResponseGenericDTO<>(true, "Found", testAmbit);
        when(ambitService.getById(1)).thenReturn(response);

        mockMvc.perform(get("/ambits/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.ambitId").value(1))
                .andExpect(jsonPath("$.message").value("Found"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void create_ShouldReturnCreatedAmbit() throws Exception {
        ResponseGenericDTO<AmbitDTO> response = new ResponseGenericDTO<>(true, "Created", testAmbit);
        when(ambitService.create(testAmbit)).thenReturn(response);

        mockMvc.perform(post("/ambits/save-ambit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAmbit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ambitId").value(1))
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void update_ShouldReturnUpdatedResponse() throws Exception {
        ResponseGenericDTO<Void> response = new ResponseGenericDTO<>(true, "Updated", null);
        when(ambitService.update(1, testAmbit)).thenReturn(response);

        mockMvc.perform(put("/ambits/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAmbit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_ShouldReturnDeletedResponse() throws Exception {
        ResponseGenericDTO<Void> response = new ResponseGenericDTO<>(true, "Deleted", null);
        when(ambitService.delete(1)).thenReturn(response);

        mockMvc.perform(delete("/ambits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted"))
                .andExpect(jsonPath("$.success").value(true));
    }
}
