package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
@Tag(name = "Torneos", description = "Operaciones CRUD para torneos")
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    @Operation(summary = "Listar todos los torneos no eliminados")
    public ResponseEntity<ResponseGenericDTO<List<TournamentDTO>>> getAll() {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Torneos cargados correctamente", tournamentService.getAll())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener torneo por ID")
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

    @GetMapping("/ambit")
    @Operation(summary = "Buscar torneos por ámbito (ambitId o ambitName)")
    public ResponseEntity<ResponseGenericDTO<List<TournamentDTO>>> getByAmbit(
            @RequestParam(required = false) Integer ambitId,
            @RequestParam(required = false) String ambitName
    ) {
        List<TournamentDTO> results = tournamentService.getTournamentsByAmbit(ambitId, ambitName);
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Torneos por ámbito cargados correctamente", results)
        );
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo torneo")
    public ResponseEntity<ResponseGenericDTO<TournamentDTO>> create(@RequestBody TournamentDTO dto) {
        TournamentDTO created = tournamentService.create(dto);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Torneo creado correctamente", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un torneo existente")
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
    @Operation(summary = "Eliminar lógicamente un torneo")
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

