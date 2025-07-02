package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.service.ResultService;
import com.bowlingpoints.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final TournamentService tournamentService;

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


    //todo evento
    @GetMapping("/by-gender")
    public ResponseEntity<ResponseGenericDTO<Map<String, List<PlayerResultSummaryDTO>>>> getResultsByGender(
            @RequestParam(name = "tournamentId", required = true) Integer tournamentId
    ) {
        Map<String, List<PlayerResultSummaryDTO>> data = resultService.getTournamentResultsByGender(tournamentId);
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Resultados agrupados por género", data)
        );
    }

    // list-toneos segun ambito
    @GetMapping("/by-ambit")
    public ResponseEntity<ResponseGenericDTO<List<TournamentDTO>>> getTournamentsByAmbit(
            @RequestParam(required = false) Integer ambitId,
            @RequestParam(required = false) String ambitName
    ) {
        List<TournamentDTO> data = tournamentService.getTournamentsByAmbit(ambitId, ambitName);
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Torneos cargados correctamente", data)
        );
    }

    // Resumen de torneos
    @GetMapping("/tournament-summary")
    public ResponseEntity<ResponseGenericDTO<TournamentSummaryDTO>> getTournamentSummary(
            @RequestParam Integer tournamentId
    ) {
        TournamentSummaryDTO dto = tournamentService.getTournamentSummary(tournamentId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resumen de torneo cargado", dto));
    }

    //detalle torneo
    @GetMapping("/table")
    public ResponseEntity<ResponseGenericDTO<List<PlayerResultTableDTO>>> getResultsTable(
            @RequestParam Integer tournamentId,
            @RequestParam Integer modalityId
    ) {
        List<PlayerResultTableDTO> result = resultService.getPlayerResultsForTable(tournamentId, modalityId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Tabla de resultados", result));
    }

}
