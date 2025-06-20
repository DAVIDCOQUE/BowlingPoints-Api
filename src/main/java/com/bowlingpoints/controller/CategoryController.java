package com.bowlingpoints.controller;

import com.bowlingpoints.dto.CategoryDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ✅ Obtener todas las categorías
    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<CategoryDTO>>> getAll() {
        List<CategoryDTO> categories = categoryService.getAll();
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Categorías obtenidas correctamente", categories)
        );
    }

    // ✅ Obtener categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<CategoryDTO>> getById(@PathVariable Integer id) {
        CategoryDTO dto = categoryService.getById(id);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Categoría no encontrada", null));
        }
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Categoría obtenida correctamente", dto)
        );
    }

    // ✅ Crear una nueva categoría
    @PostMapping
    public ResponseEntity<ResponseGenericDTO<CategoryDTO>> create(@RequestBody CategoryDTO dto) {
        CategoryDTO created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseGenericDTO<>(true, "Categoría creada correctamente", created));
    }

    // ✅ Actualizar una categoría
    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody CategoryDTO dto) {
        boolean updated = categoryService.update(id, dto);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Categoría no encontrada", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Categoría actualizada correctamente", null));
    }

    // ✅ Eliminar una categoría
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        boolean deleted = categoryService.delete(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Categoría no encontrada", null));
        }
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Categoría eliminada correctamente", null));
    }
}
