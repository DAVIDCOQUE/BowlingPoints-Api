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
 * Controlador REST para operaciones sobre resultados, rankings y torneos.
 */
@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final TournamentService tournamentService;

    /**
     * Obtiene todos los resultados registrados en el sistema.
     * @return Lista de resultados con detalles de jugador, torneo, puntaje, etc.
     */
    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<ResultDTO>>> getAll() {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Resultados cargados correctamente", resultService.getAll())
        );
    }

    /**
     * Obtiene el detalle de un resultado por su ID.
     * @param id ID del resultado a consultar.
     * @return Detalle del resultado o error 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<ResultDTO>> getById(@PathVariable Integer id) {
        ResultDTO dto = resultService.getById(id);
        return dto != null
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultado encontrado", dto))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Resultado no encontrado", null));
    }

    /**
     * Crea un nuevo resultado en el sistema.
     * @param dto Datos del resultado a registrar.
     * @return Resultado creado con datos completos.
     */
    @PostMapping
    public ResponseEntity<ResponseGenericDTO<ResultDTO>> create(@RequestBody ResultDTO dto) {
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Resultado creado correctamente", resultService.create(dto))
        );
    }

    /**
     * Actualiza un resultado existente por ID.
     * @param id ID del resultado.
     * @param dto Datos actualizados.
     * @return Respuesta de éxito o error si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody ResultDTO dto) {
        boolean updated = resultService.update(id, dto);
        return updated
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultado actualizado correctamente", null))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Resultado no encontrado", null));
    }

    /**
     * Elimina un resultado por ID.
     * @param id ID del resultado.
     * @return Respuesta de éxito o error si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        boolean deleted = resultService.delete(id);
        return deleted
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resultado eliminado correctamente", null))
                : ResponseEntity.status(404).body(new ResponseGenericDTO<>(false, "Resultado no encontrado", null));
    }

    /**
     * Agrupa los resultados de un torneo por género y jugador, mostrando totales y promedios.
     * @param tournamentId ID del torneo.
     * @return Resultados agrupados por género.
     */
    @GetMapping("/by-gender")
    public ResponseEntity<ResponseGenericDTO<Map<String, List<PlayerResultSummaryDTO>>>> getResultsByGender(
            @RequestParam(name = "tournamentId", required = true) Integer tournamentId
    ) {
        Map<String, List<PlayerResultSummaryDTO>> data = resultService.getTournamentResultsByGender(tournamentId);
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Resultados agrupados por género", data)
        );
    }

    /**
     * Lista todos los torneos según el ámbito indicado.
     * @param ambitId (opcional) ID del ámbito.
     * @param ambitName (opcional) Nombre del ámbito.
     * @return Lista de torneos.
     */
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

    /**
     * Devuelve el resumen global de un torneo (totales, promedios, jugadores, etc).
     * @param tournamentId ID del torneo.
     * @return Resumen del torneo.
     */
    @GetMapping("/tournament-summary")
    public ResponseEntity<ResponseGenericDTO<TournamentSummaryDTO>> getTournamentSummary(
            @RequestParam Integer tournamentId
    ) {
        TournamentSummaryDTO dto = tournamentService.getTournamentSummary(tournamentId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Resumen de torneo cargado", dto));
    }

    /**
     * Devuelve la tabla detallada de resultados de un torneo y modalidad.
     * @param tournamentId ID del torneo.
     * @param modalityId ID de la modalidad.
     * @return Tabla de resultados con totales y promedios.
     */
    @GetMapping("/table")
    public ResponseEntity<ResponseGenericDTO<List<PlayerResultTableDTO>>> getResultsTable(
            @RequestParam Integer tournamentId,
            @RequestParam Integer modalityId
    ) {
        List<PlayerResultTableDTO> result = resultService.getPlayerResultsForTable(tournamentId, modalityId);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Tabla de resultados", result));
    }

    /**
     * Obtiene el ranking completo de todos los jugadores ordenados por promedio.
     * Útil para la pantalla de todos los jugadores rankeados.
     * @return Ranking de todos los jugadores.
     */
    @GetMapping("/all-player-ranking")
    public ResponseEntity<ResponseGenericDTO<List<PlayerRankingDTO>>> getAllPlayerRanking() {
        List<PlayerRankingDTO> ranking = resultService.getAllPlayersByAvgScore();
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Ranking de todos los jugadores cargado correctamente", ranking)
        );
    }

}
