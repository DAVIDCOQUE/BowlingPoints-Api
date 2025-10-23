package com.bowlingpoints.controller;

import com.bowlingpoints.dto.BranchDTO;
import com.bowlingpoints.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    /** Crea una nueva rama. */
    @PostMapping
    public ResponseEntity<BranchDTO> create(@RequestBody BranchDTO dto) {
        return ResponseEntity.ok(branchService.create(dto));
    }

    /** Obtiene todas las ramas (activas e inactivas). */
    @GetMapping
    public ResponseEntity<List<BranchDTO>> getAll() {
        return ResponseEntity.ok(branchService.getAll());
    }

    /** Obtiene solo las ramas activas. */
    @GetMapping("/active")
    public ResponseEntity<List<BranchDTO>> getActive() {
        return ResponseEntity.ok(branchService.getActive());
    }

    /** Obtiene una rama específica por su ID. */
    @GetMapping("/{id}")
    public ResponseEntity<BranchDTO> getById(@PathVariable Integer id) {
        BranchDTO dto = branchService.getById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    /** Actualiza una rama existente. */
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Integer id, @RequestBody BranchDTO dto) {
        boolean updated = branchService.update(id, dto);
        if (!updated) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Rama actualizada correctamente.");
    }

    /** Elimina una rama (soft delete). */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        boolean deleted = branchService.delete(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Rama eliminada correctamente (soft delete).");
    }
}
