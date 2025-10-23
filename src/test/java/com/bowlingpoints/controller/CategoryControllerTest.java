package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtAuthenticationFilter;
import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.CategoryService;
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

@WebMvcTest(CategoryController.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new CategoryDTO();
        testCategory.setCategoryId(1);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setStatus(true);
    }

    @Test
    void getAll_ShouldReturnCategories() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of(testCategory));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categorías obtenidas correctamente"))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Test Category"));
    }

    @Test
    void getById_ShouldReturnCategory_WhenExists() throws Exception {
        when(categoryService.getById(1)).thenReturn(testCategory);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categoría obtenida correctamente"))
                .andExpect(jsonPath("$.data.categoryId").value(1));
    }

    @Test
    void getById_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
        when(categoryService.getById(1)).thenReturn(null);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Categoría no encontrada"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void create_ShouldReturnCreatedCategory() throws Exception {
        when(categoryService.create(testCategory)).thenReturn(testCategory);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categoría creada correctamente"))
                .andExpect(jsonPath("$.data.categoryId").value(1));
    }

    @Test
    void update_ShouldReturnSuccess_WhenCategoryExists() throws Exception {
        when(categoryService.update(1, testCategory)).thenReturn(true);

        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categoría actualizada correctamente"));
    }

    @Test
    void update_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
        when(categoryService.update(1, testCategory)).thenReturn(false);

        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Categoría no encontrada"));
    }

    @Test
    void delete_ShouldReturnSuccess_WhenCategoryExists() throws Exception {
        when(categoryService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categoría eliminada correctamente"));
    }

    @Test
    void delete_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
        when(categoryService.delete(1)).thenReturn(false);

        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Categoría no encontrada"));
    }
}
