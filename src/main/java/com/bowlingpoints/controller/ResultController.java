package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.ResultDTO;
import com.bowlingpoints.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<ResultDTO>>> getAll() {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Resultados cargados correctamente", resultService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<ResultDTO>> getById(@PathVariable Integer id) {
        ResultDTO dto = resultService.getById(id);
        return dto != null
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultado encontrado", dto))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Resultado no encontrado", null));
    }

    @PostMapping
    public ResponseEntity<ResponseGenericDTO<ResultDTO>> create(@RequestBody ResultDTO dto) {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Resultado creado correctamente", resultService.create(dto))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody ResultDTO dto) {
        boolean updated = resultService.update(id, dto);
        return updated
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultado actualizado correctamente", null))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Resultado no encontrado", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        boolean deleted = resultService.delete(id);
        return deleted
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultado eliminado correctamente", null))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Resultado no encontrado", null));
    }
}
