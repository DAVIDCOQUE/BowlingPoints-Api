package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.ModalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar modalidades.
 */
@RestController
@RequestMapping("/modalities")
@RequiredArgsConstructor
public class ModalityController {

    private final ModalityService modalityService;

    /**
     * Lista todas las modalidades no eliminadas.
     */
    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<ModalityDTO>>> getAll() {
        return ResponseEntity.ok(modalityService.getAll());
    }

    /**
     * Lista solo las modalidades activas.
     */
    @GetMapping("/actives")
    public ResponseEntity<ResponseGenericDTO<List<ModalityDTO>>> getAllActives() {
        return ResponseEntity.ok(modalityService.getAllActives());
    }

    /**
     * Obtiene una modalidad por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<ModalityDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(modalityService.getById(id));
    }

    /**
     * Crea una nueva modalidad.
     */
    @PostMapping
    public ResponseEntity<ResponseGenericDTO<ModalityDTO>> create(@RequestBody ModalityDTO dto) {
        return ResponseEntity.ok(modalityService.create(dto));
    }

    /**
     * Actualiza una modalidad existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody ModalityDTO dto) {
        return ResponseEntity.ok(modalityService.update(id, dto));
    }

    /**
     * Elimina suavemente una modalidad.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(modalityService.delete(id));
    }
}
