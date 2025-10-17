package com.bowlingpoints.controller;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de categorías.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Lista todas las categorías no eliminadas.
     */
    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<CategoryDTO>>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    /**
     * Lista todas las categorías activas.
     */
    @GetMapping("/actives")
    public ResponseEntity<ResponseGenericDTO<List<CategoryDTO>>> getAllActives() {
        return ResponseEntity.ok(categoryService.getAllActives());
    }

    /**
     * Obtiene una categoría por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<CategoryDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * Crea una nueva categoría.
     */
    @PostMapping
    public ResponseEntity<ResponseGenericDTO<CategoryDTO>> create(@RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.create(dto));
    }

    /**
     * Actualiza una categoría existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    /**
     * Elimina suavemente una categoría (soft delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.delete(id));
    }
}
