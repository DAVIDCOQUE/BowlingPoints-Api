package com.bowlingpoints.controller;

import com.bowlingpoints.dto.TournamentRegistrationDTO;
import com.bowlingpoints.service.TournamentRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class TournamentRegistrationController {

    private final TournamentRegistrationService registrationService;

    //  Crear nueva inscripción
    @PostMapping
    public ResponseEntity<TournamentRegistrationDTO> create(@RequestBody TournamentRegistrationDTO dto) {
        return ResponseEntity.ok(registrationService.create(dto));
    }

    //  Obtener todas las inscripciones activas
    @GetMapping
    public ResponseEntity<List<TournamentRegistrationDTO>> getAll() {
        return ResponseEntity.ok(registrationService.getAll());
    }

    //  Obtener inscripciones activas por torneo
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<TournamentRegistrationDTO>> getByTournament(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(registrationService.getByTournament(tournamentId));
    }

    //  Obtener inscripciones activas por persona
    @GetMapping("/person/{personId}")
    public ResponseEntity<List<TournamentRegistrationDTO>> getByPerson(@PathVariable Integer personId) {
        return ResponseEntity.ok(registrationService.getByPerson(personId));
    }

    //  Obtener una inscripción por ID
    @GetMapping("/{id}")
    public ResponseEntity<TournamentRegistrationDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(registrationService.getById(id));
    }

    //  Actualizar inscripción
    @PutMapping("/{id}")
    public ResponseEntity<TournamentRegistrationDTO> update(
            @PathVariable Integer id,
            @RequestBody TournamentRegistrationDTO dto) {
        return ResponseEntity.ok(registrationService.update(id, dto));
    }

    //  Eliminar inscripción (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        boolean deleted = registrationService.delete(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Registro eliminado correctamente");
    }
}
