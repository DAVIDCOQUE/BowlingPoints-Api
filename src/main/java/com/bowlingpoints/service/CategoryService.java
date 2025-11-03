package com.bowlingpoints.service;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.Category;
import com.bowlingpoints.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones CRUD sobre Categorías.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Lista todas las categorías no eliminadas.
     */
    public ResponseGenericDTO<List<CategoryDTO>> getAll() {
        List<CategoryDTO> result = categoryRepository
                .findAllByDeletedAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new ResponseGenericDTO<>(true, "Categorías cargadas correctamente", result);
    }

    /**
     * Lista las categorías activas (status = true).
     */
    public ResponseGenericDTO<List<CategoryDTO>> getAllActives() {
        List<CategoryDTO> result = categoryRepository
                .findAllByDeletedAtIsNullAndStatusTrueOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new ResponseGenericDTO<>(true, "Categorías activas cargadas correctamente", result);
    }

    /**
     * Busca una categoría por su ID.
     */
    public ResponseGenericDTO<CategoryDTO> getById(Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category
                .filter(c -> c.getDeletedAt() == null)
                .map(c -> new ResponseGenericDTO<>(true, "Categoría encontrada", toDTO(c)))
                .orElseGet(() -> new ResponseGenericDTO<>(false, "Categoría no encontrada", null));
    }

    /**
     * Crea una nueva categoría.
     */
    public ResponseGenericDTO<CategoryDTO> create(CategoryDTO dto) {
        Category entity = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category saved = categoryRepository.save(entity);
        return new ResponseGenericDTO<>(true, "Categoría creada correctamente", toDTO(saved));
    }

    /**
     * Actualiza una categoría existente.
     */
    public ResponseGenericDTO<Void> update(Integer id, CategoryDTO dto) {
        Optional<Category> optional = categoryRepository.findById(id);

        if (optional.isPresent()) {
            Category existing = optional.get();
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            existing.setStatus(dto.getStatus());
            existing.setUpdatedAt(LocalDateTime.now());

            categoryRepository.save(existing);
            return new ResponseGenericDTO<>(true, "Categoría actualizada correctamente", null);
        }

        return new ResponseGenericDTO<>(false, "Categoría no encontrada", null);
    }

    /**
     * Elimina suavemente una categoría (soft delete).
     */
    public ResponseGenericDTO<Void> delete(Integer id) {
        Optional<Category> optional = categoryRepository.findById(id);

        if (optional.isPresent()) {
            Category existing = optional.get();
            existing.setDeletedAt(LocalDateTime.now());
            existing.setUpdatedAt(LocalDateTime.now());

            categoryRepository.save(existing);
            return new ResponseGenericDTO<>(true, "Categoría eliminada correctamente", null);
        }

        return new ResponseGenericDTO<>(false, "Categoría no encontrada", null);
    }

    /**
     * Convierte entidad Category a DTO.
     */
    private CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .categoryId(c.getCategoryId())
                .name(c.getName())
                .description(c.getDescription())
                .status(c.getStatus())
                .build();
    }
}
