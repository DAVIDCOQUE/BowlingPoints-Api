package com.bowlingpoints.controller;


import com.bowlingpoints.dto.BranchDTO;
import com.bowlingpoints.service.BranchService;
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
 * 🧪 Tests para BranchController
 */
@WebMvcTest(controllers = BranchController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BranchService branchService;

    @Autowired
    private ObjectMapper objectMapper;

    private BranchDTO branchDTO;

    @BeforeEach
    void setUp() {
        branchDTO = BranchDTO.builder()
                .branchId(1)
                .name("Sucursal Cali")
                .status(true)
                .build();
    }

    // ===============================
    // POST /branches
    // ===============================
    @Test
    void create_ShouldReturnCreatedBranch() throws Exception {
        Mockito.when(branchService.create(any(BranchDTO.class))).thenReturn(branchDTO);

        mockMvc.perform(post("/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sucursal Cali"));
    }

    // ===============================
    // GET /branches
    // ===============================
    @Test
    void getAll_ShouldReturnListOfBranches() throws Exception {
        Mockito.when(branchService.getAll()).thenReturn(List.of(branchDTO));

        mockMvc.perform(get("/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sucursal Cali"));
    }

    // ===============================
    // GET /branches/active
    // ===============================
    @Test
    void getActive_ShouldReturnActiveBranches() throws Exception {
        Mockito.when(branchService.getActive()).thenReturn(List.of(branchDTO));

        mockMvc.perform(get("/branches/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(true));
    }

    // ===============================
    // GET /branches/{id}
    // ===============================
    @Test
    void getById_ShouldReturnBranch_WhenExists() throws Exception {
        Mockito.when(branchService.getById(1)).thenReturn(branchDTO);

        mockMvc.perform(get("/branches/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sucursal Cali"));
    }

    @Test
    void getById_ShouldReturnNotFound_WhenBranchDoesNotExist() throws Exception {
        Mockito.when(branchService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/branches/{id}", 99))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // PUT /branches/{id}
    // ===============================
    @Test
    void update_ShouldReturnOk_WhenBranchExists() throws Exception {
        Mockito.when(branchService.update(eq(1), any(BranchDTO.class))).thenReturn(true);

        mockMvc.perform(put("/branches/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Rama actualizada correctamente."));
    }

    @Test
    void update_ShouldReturnNotFound_WhenBranchNotExists() throws Exception {
        Mockito.when(branchService.update(eq(99), any(BranchDTO.class))).thenReturn(false);

        mockMvc.perform(put("/branches/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchDTO)))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // DELETE /branches/{id}
    // ===============================
    @Test
    void delete_ShouldReturnOk_WhenBranchExists() throws Exception {
        Mockito.when(branchService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/branches/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("Rama eliminada correctamente (soft delete)."));
    }

    @Test
    void delete_ShouldReturnNotFound_WhenBranchNotExists() throws Exception {
        Mockito.when(branchService.delete(99)).thenReturn(false);

        mockMvc.perform(delete("/branches/{id}", 99))
                .andExpect(status().isNotFound());
    }
}

