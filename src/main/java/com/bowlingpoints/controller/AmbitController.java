package com.bowlingpoints.controller;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.AmbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar ambits.
 */
@RestController
@RequestMapping("/ambits")
@RequiredArgsConstructor
public class AmbitController {

    private final AmbitService ambitService;

    /**
     * Obtiene todos los ambits no eliminados.
     */
    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<AmbitDTO>>> getAll() {
        return ResponseEntity.ok(ambitService.getAll());
    }

    /**
     * Obtiene un ambit por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<AmbitDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ambitService.getById(id));
    }

    /**
     * Crea un nuevo ambit.
     */
    @PostMapping
    public ResponseEntity<ResponseGenericDTO<AmbitDTO>> create(@RequestBody AmbitDTO dto) {
        return ResponseEntity.ok(ambitService.create(dto));
    }

    /**
     * Actualiza un ambit existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody AmbitDTO dto) {
        return ResponseEntity.ok(ambitService.update(id, dto));
    }

    /**
     * Elimina suavemente un ambit.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(ambitService.delete(id));
    }

    /**
     * Obtiene solo los ambits activos.
     */
    @GetMapping("/actives")
    public ResponseEntity<ResponseGenericDTO<List<AmbitDTO>>> getAllActives() {
        return ResponseEntity.ok(ambitService.getAllActives());
    }

    /**
     * Obtiene ambits que tienen torneos asociados.
     */
    @GetMapping("/with-tournaments")
    public ResponseEntity<ResponseGenericDTO<List<AmbitDTO>>> getAmbitsWithTournaments() {
        return ResponseEntity.ok(ambitService.getAmbitsWithTournaments());
    }
}
