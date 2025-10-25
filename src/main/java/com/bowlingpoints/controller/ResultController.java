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

    // Resultados filtrados por torneo, rama y ronda
    @GetMapping("/filter")
    public ResponseEntity<List<ResultDTO>> getFilteredResults(
            @RequestParam Integer tournamentId,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer roundNumber
    ) {
        List<ResultDTO> results = resultService.getResultsByTournamentFiltered(tournamentId, branchId, roundNumber);
        return ResponseEntity.ok(results);
    }

    // --------------------------------------------------
    // Tabla de resultados del torneo por modalidad y ronda
    // --------------------------------------------------

    @GetMapping("/tournament-table")
    public ResponseEntity<TournamentResultsResponseDTO> getTournamentResultsTable(
            @RequestParam Integer tournamentId,
            @RequestParam Integer modalityId,
            @RequestParam(required = false) Integer roundNumber
    ) {
        return ResponseEntity.ok(resultService.getTournamentResultsTable(tournamentId, modalityId, roundNumber));
    }

    @GetMapping("/by-modality")
    public ResponseEntity<TournamentResultsResponseDTO> getResultsByModality(
            @RequestParam Integer tournamentId,
            @RequestParam(required = false) Integer roundNumber,
            @RequestParam(required = false) Integer branchId
    ) {
        TournamentResultsResponseDTO response = resultService.getResultsByModality(tournamentId, roundNumber, branchId);
        return ResponseEntity.ok(response);
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

}
