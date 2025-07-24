package com.bowlingpoints.service;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.entity.Category;
import com.bowlingpoints.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAll() {
        return categoryRepository.findAllByDeletedAtIsNullOrderByNameAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getById(Integer id) {
        return categoryRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .map(this::toDTO)
                .orElse(null);
    }

    public CategoryDTO create(CategoryDTO dto) {
        Category entity = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();
        categoryRepository.save(entity);
        return toDTO(entity);
    }

    public boolean update(Integer id, CategoryDTO dto) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setDescription(dto.getDescription());
                    existing.setStatus(dto.getStatus());
                    existing.setUpdatedAt(LocalDateTime.now());
                    categoryRepository.save(existing);
                    return true;
                }).orElse(false);
    }

    public boolean delete(Integer id) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setDeletedAt(LocalDateTime.now());
                    categoryRepository.save(existing);
                    return true;
                }).orElse(false);
    }

    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setName(c.getName());
        dto.setCategoryId(c.getCategoryId());
        dto.setDescription(c.getDescription());
        dto.setStatus(c.getStatus());
        return dto;
    }
}
