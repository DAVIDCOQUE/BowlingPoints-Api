package com.bowlingpoints.controller;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.service.ResultService;
import com.bowlingpoints.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de resultados, rankings y resúmenes de torneos.
 */
@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final TournamentService tournamentService;

    // --------------------------------------------------
    // CRUD de Resultados
    // --------------------------------------------------

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<ResultDTO>>> getAll() {
        List<ResultDTO> results = resultService.getAll();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultados cargados correctamente", results));
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
        ResultDTO created = resultService.create(dto);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultado creado correctamente", created));
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

    // --------------------------------------------------
    // Estadísticas y Agrupaciones
    // --------------------------------------------------

    @GetMapping("/by-gender")
    public ResponseEntity<ResponseGenericDTO<Map<String, List<PlayerResultSummaryDTO>>>> getResultsByGender(
            @RequestParam Integer tournamentId) {
        Map<String, List<PlayerResultSummaryDTO>> data = resultService.getTournamentResultsByGender(tournamentId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultados agrupados por género", data));
    }

    @GetMapping("/table")
    public ResponseEntity<ResponseGenericDTO<List<PlayerResultTableDTO>>> getResultsTable(
            @RequestParam Integer tournamentId,
            @RequestParam Integer modalityId) {
        List<PlayerResultTableDTO> table = resultService.getPlayerResultsForTable(tournamentId, modalityId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Tabla de resultados", table));
    }

    @GetMapping("/all-player-ranking")
    public ResponseEntity<ResponseGenericDTO<List<DashboardPlayerDTO>>> getAllPlayerRanking() {
        List<DashboardPlayerDTO> ranking = resultService.getAllPlayersByAvgScore();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Ranking cargado correctamente", ranking));
    }

    // --------------------------------------------------
    // Torneos (uso secundario desde resultados)
    // --------------------------------------------------

    @GetMapping("/by-ambit")
    public ResponseEntity<ResponseGenericDTO<List<TournamentDTO>>> getTournamentsByAmbit(
            @RequestParam(required = false) Integer ambitId,
            @RequestParam(required = false) String ambitName) {
        List<TournamentDTO> data = tournamentService.getTournamentsByAmbit(ambitId, ambitName);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Torneos cargados correctamente", data));
    }

    @GetMapping("/tournament-summary")
    public ResponseEntity<ResponseGenericDTO<TournamentSummaryDTO>> getTournamentSummary(
            @RequestParam Integer tournamentId) {
        TournamentSummaryDTO summary = tournamentService.getTournamentSummary(tournamentId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resumen del torneo cargado", summary));
    }
}
