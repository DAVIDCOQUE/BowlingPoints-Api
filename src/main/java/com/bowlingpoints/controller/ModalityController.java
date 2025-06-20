package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ModalityDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.ModalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modalities")
@RequiredArgsConstructor
public class ModalityController {

    private final ModalityService modalityService;

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<ModalityDTO>>> getAll() {
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Modalidades cargadas correctamente", modalityService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<ModalityDTO>> getById(@PathVariable Integer id) {
        ModalityDTO dto = modalityService.getById(id);
        return dto != null
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Modalidad encontrada", dto))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ResponseGenericDTO<ModalityDTO>> create(@RequestBody ModalityDTO dto) {
        ModalityDTO saved = modalityService.create(dto);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Modalidad creada correctamente", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody ModalityDTO dto) {
        boolean updated = modalityService.update(id, dto);
        return updated
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Modalidad actualizada", null))
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        boolean deleted = modalityService.delete(id);
        return deleted
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Modalidad eliminada", null))
                : ResponseEntity.notFound().build();
    }
}
