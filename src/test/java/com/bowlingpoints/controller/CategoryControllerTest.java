package com.bowlingpoints.controller;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.CategoryService;
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


@WebMvcTest(controllers = CategoryController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO categoryDTO;
    private ResponseGenericDTO<CategoryDTO> responseSingle;
    private ResponseGenericDTO<List<CategoryDTO>> responseList;
    private ResponseGenericDTO<Void> responseVoid;

    @BeforeEach
    void setUp() {
        categoryDTO = CategoryDTO.builder()
                .categoryId(1)
                .name("Senior")
                .description("Jugadores de categoría senior")
                .status(true)
                .build();

        responseSingle = new ResponseGenericDTO<>(true, "OK", categoryDTO);
        responseList = new ResponseGenericDTO<>(true, "OK", List.of(categoryDTO));
        responseVoid = new ResponseGenericDTO<>(true, "OK", null);
    }

    // ===============================
    // GET /categories
    // ===============================
    @Test
    void getAll_ShouldReturnListOfCategories() throws Exception {
        Mockito.when(categoryService.getAll()).thenReturn(responseList);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Senior"));
    }

    // ===============================
    // GET /categories/actives
    // ===============================
    @Test
    void getAllActives_ShouldReturnActiveCategories() throws Exception {
        Mockito.when(categoryService.getAllActives()).thenReturn(responseList);

        mockMvc.perform(get("/categories/actives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value(true));
    }

    // ===============================
    // GET /categories/{id}
    // ===============================
    @Test
    void getById_ShouldReturnCategory() throws Exception {
        Mockito.when(categoryService.getById(1)).thenReturn(responseSingle);

        mockMvc.perform(get("/categories/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Senior"));
    }

    // ===============================
    // POST /categories
    // ===============================
    @Test
    void create_ShouldReturnCreatedCategory() throws Exception {
        Mockito.when(categoryService.create(any(CategoryDTO.class))).thenReturn(responseSingle);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Senior"));
    }

    // ===============================
    // PUT /categories/{id}
    // ===============================
    @Test
    void update_ShouldReturnOkResponse() throws Exception {
        Mockito.when(categoryService.update(eq(1), any(CategoryDTO.class))).thenReturn(responseVoid);

        mockMvc.perform(put("/categories/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    // ===============================
    // DELETE /categories/{id}
    // ===============================
    @Test
    void delete_ShouldReturnOkResponse() throws Exception {
        Mockito.when(categoryService.delete(1)).thenReturn(responseVoid);

        mockMvc.perform(delete("/categories/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"));
    }
}
