package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.RoundDTO;
import com.bowlingpoints.service.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundService roundService;

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<RoundDTO>>> getAll() {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Rondas cargadas correctamente", roundService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<RoundDTO>> getById(@PathVariable Integer id) {
        RoundDTO dto = roundService.getById(id);
        return dto != null
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Ronda encontrada", dto))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Ronda no encontrada", null));
    }

    @PostMapping
    public ResponseEntity<ResponseGenericDTO<RoundDTO>> create(@RequestBody RoundDTO dto) {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Ronda creada correctamente", roundService.create(dto))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody RoundDTO dto) {
        boolean updated = roundService.update(id, dto);
        return updated
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Ronda actualizada correctamente", null))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Ronda no encontrada", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        boolean deleted = roundService.delete(id);
        return deleted
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Ronda eliminada correctamente", null))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Ronda no encontrada", null));
    }
}
