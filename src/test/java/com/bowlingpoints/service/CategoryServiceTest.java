package com.bowlingpoints.service;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.Category;
import com.bowlingpoints.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para CategoryService.
 */
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category = Category.builder()
                .categoryId(1)
                .name("Elite")
                .description("Categoría para jugadores profesionales")
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();

        categoryDTO = CategoryDTO.builder()
                .categoryId(1)
                .name("Elite")
                .description("Categoría para jugadores profesionales")
                .status(true)
                .build();
    }

    // ----------------------------------------------------------------------
    // getAll
    // ----------------------------------------------------------------------
    @Test
    void getAll_ShouldReturnCategories_WhenExist() {
        when(categoryRepository.findAllByDeletedAtIsNullOrderByNameAsc())
                .thenReturn(List.of(category));

        ResponseGenericDTO<List<CategoryDTO>> response = categoryService.getAll();

        assertTrue(response.getSuccess());
        assertEquals("Categorías cargadas correctamente", response.getMessage());
        assertEquals(1, response.getData().size());
        assertEquals("Elite", response.getData().get(0).getName());
    }

    // ----------------------------------------------------------------------
    // getAllActives
    // ----------------------------------------------------------------------
    @Test
    void getAllActives_ShouldReturnActiveCategories_WhenExist() {
        when(categoryRepository.findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc())
                .thenReturn(List.of(category));

        ResponseGenericDTO<List<CategoryDTO>> response = categoryService.getAllActives();

        assertTrue(response.getSuccess());
        assertEquals("Categorías activas cargadas correctamente", response.getMessage());
        assertEquals(1, response.getData().size());
        assertTrue(response.getData().get(0).getStatus());
    }

    // ----------------------------------------------------------------------
    // getById
    // ----------------------------------------------------------------------
    @Test
    void getById_ShouldReturnCategory_WhenExistsAndNotDeleted() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        ResponseGenericDTO<CategoryDTO> response = categoryService.getById(1);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals("Elite", response.getData().getName());
        assertEquals("Categoría encontrada", response.getMessage());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenCategoryDeleted() {
        category.setDeletedAt(LocalDateTime.now());
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        ResponseGenericDTO<CategoryDTO> response = categoryService.getById(1);

        assertFalse(response.getSuccess());
        assertNull(response.getData());
        assertEquals("Categoría no encontrada", response.getMessage());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenDoesNotExist() {
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        ResponseGenericDTO<CategoryDTO> response = categoryService.getById(999);

        assertFalse(response.getSuccess());
        assertNull(response.getData());
        assertEquals("Categoría no encontrada", response.getMessage());
    }

    // ----------------------------------------------------------------------
    // create
    // ----------------------------------------------------------------------
    @Test
    void create_ShouldSaveAndReturnCategoryDTO() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        ResponseGenericDTO<CategoryDTO> response = categoryService.create(categoryDTO);

        assertTrue(response.getSuccess());
        assertEquals("Categoría creada correctamente", response.getMessage());
        assertEquals("Elite", response.getData().getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    // ----------------------------------------------------------------------
    // update
    // ----------------------------------------------------------------------
    @Test
    void update_ShouldModifyCategory_WhenExists() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO updatedDTO = CategoryDTO.builder()
                .name("Junior")
                .description("Nueva descripción")
                .status(false)
                .build();

        ResponseGenericDTO<Void> response = categoryService.update(1, updatedDTO);

        assertTrue(response.getSuccess());
        assertEquals("Categoría actualizada correctamente", response.getMessage());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenCategoryDoesNotExist() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        ResponseGenericDTO<Void> response = categoryService.update(99, categoryDTO);

        assertFalse(response.getSuccess());
        assertEquals("Categoría no encontrada", response.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ----------------------------------------------------------------------
    // delete
    // ----------------------------------------------------------------------
    @Test
    void delete_ShouldSoftDeleteCategory_WhenExists() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        ResponseGenericDTO<Void> response = categoryService.delete(1);

        assertTrue(response.getSuccess());
        assertEquals("Categoría eliminada correctamente", response.getMessage());
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertNotNull(category.getDeletedAt());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenCategoryDoesNotExist() {
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        ResponseGenericDTO<Void> response = categoryService.delete(999);

        assertFalse(response.getSuccess());
        assertEquals("Categoría no encontrada", response.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
