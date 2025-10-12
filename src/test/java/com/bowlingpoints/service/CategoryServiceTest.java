package com.bowlingpoints.service;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.entity.Category;
import com.bowlingpoints.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .categoryId(1)
                .name("Test Category")
                .description("Test Description")
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setCategoryId(1);
        testCategoryDTO.setName("Test Category");
        testCategoryDTO.setDescription("Test Description");
        testCategoryDTO.setStatus(true);
    }

    @Test
    void getAll_ShouldReturnAllCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(
            testCategory,
            Category.builder()
                .categoryId(2)
                .name("Test Category 2")
                .description("Test Description 2")
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        when(categoryRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(categories);

        // Act
        List<CategoryDTO> result = categoryService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Category", result.get(0).getName());
        assertEquals("Test Category 2", result.get(1).getName());
    }

    @Test
    void getAll_WhenNoCategories_ShouldReturnEmptyList() {
        // Arrange
        when(categoryRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(Collections.emptyList());

        // Act
        List<CategoryDTO> result = categoryService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getById_WhenCategoryExists_ShouldReturnCategory() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));

        // Act
        CategoryDTO result = categoryService.getById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testCategory.getName(), result.getName());
        assertEquals(testCategory.getDescription(), result.getDescription());
        assertEquals(testCategory.getStatus(), result.getStatus());
    }

    @Test
    void getById_WhenCategoryDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        CategoryDTO result = categoryService.getById(999);

        // Assert
        assertNull(result);
    }

    @Test
    void getById_WhenCategoryIsDeleted_ShouldReturnNull() {
        // Arrange
        Category deletedCategory = Category.builder()
                .categoryId(1)
                .name("Deleted Category")
                .deletedAt(LocalDateTime.now())
                .build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(deletedCategory));

        // Act
        CategoryDTO result = categoryService.getById(1);

        // Assert
        assertNull(result);
    }

    @Test
    void create_ShouldSaveAndReturnNewCategory() {
        // Arrange
        CategoryDTO newCategoryDTO = new CategoryDTO();
        newCategoryDTO.setName("New Category");
        newCategoryDTO.setDescription("New Description");
        newCategoryDTO.setStatus(true);

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setCategoryId(1);
            return saved;
        });

        // Act
        CategoryDTO result = categoryService.create(newCategoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(newCategoryDTO.getName(), result.getName());
        assertEquals(newCategoryDTO.getDescription(), result.getDescription());
        assertEquals(newCategoryDTO.getStatus(), result.getStatus());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_WhenCategoryExists_ShouldUpdateAndReturnTrue() {
        // Arrange
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setDescription("Updated Description");
        updateDTO.setStatus(false);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        boolean result = categoryService.update(1, updateDTO);

        // Assert
        assertTrue(result);
        verify(categoryRepository).save(argThat(category -> 
            category.getName().equals("Updated Name") &&
            category.getDescription().equals("Updated Description") &&
            !category.getStatus() &&
            category.getUpdatedAt() != null
        ));
    }

    @Test
    void update_WhenCategoryDoesNotExist_ShouldReturnFalse() {
        // Arrange
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Updated Name");
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = categoryService.update(999, updateDTO);

        // Assert
        assertFalse(result);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_WhenCategoryExists_ShouldSoftDeleteAndReturnTrue() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        boolean result = categoryService.delete(1);

        // Assert
        assertTrue(result);
        verify(categoryRepository).save(argThat(category -> 
            category.getDeletedAt() != null
        ));
    }

    @Test
    void delete_WhenCategoryDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = categoryService.delete(999);

        // Assert
        assertFalse(result);
        verify(categoryRepository, never()).save(any());
    }
}