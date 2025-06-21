package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<TournamentDTO>>> getAll() {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Torneos cargados correctamente", tournamentService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<TournamentDTO>> getById(@PathVariable Integer id) {
        TournamentDTO dto = tournamentService.getById(id);
        if (dto != null) {
            return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Torneo encontrado", dto));
        } else {
            return ResponseEntity.status(404).body(
                    new ResponseGenericDTO<>(false, "Torneo no encontrado", null)
            );
        }
    }

    @PostMapping
    public ResponseEntity<ResponseGenericDTO<TournamentDTO>> create(@RequestBody TournamentDTO dto) {
        TournamentDTO created = tournamentService.create(dto);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Torneo creado correctamente", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody TournamentDTO dto) {
        boolean updated = tournamentService.update(id, dto);
        if (updated) {
            return ResponseEntity.ok(
                    new ResponseGenericDTO<>(true, "Torneo actualizado correctamente", null)
            );
        } else {
            return ResponseEntity.status(404).body(
                    new ResponseGenericDTO<>(false, "Torneo no encontrado para actualizar", null)
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        boolean deleted = tournamentService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(
                    new ResponseGenericDTO<>(true, "Torneo eliminado correctamente", null)
            );
        } else {
            return ResponseEntity.status(404).body(
                    new ResponseGenericDTO<>(false, "Torneo no encontrado para eliminar", null)
            );
        }
    }
}
