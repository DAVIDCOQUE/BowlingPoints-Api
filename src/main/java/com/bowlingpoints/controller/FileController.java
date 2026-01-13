package com.bowlingpoints.controller;

import com.bowlingpoints.dto.PersonImportResponse; 
import com.bowlingpoints.service.PersonImportService;
import com.bowlingpoints.service.ResultImportService;
import com.bowlingpoints.service.TeamPersonImportService;
import com.bowlingpoints.service.TournamentRegistrationImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final PersonImportService personImportService;

    private final TeamPersonImportService teamPersonimportService;

    private final ResultImportService resultImportService;

    private final TournamentRegistrationImportService tournamentRegistrationImportService;

    @PostMapping("/persons")
    public ResponseEntity<?> importPersons(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor seleccione un archivo válido.");
        }
        try {
            PersonImportResponse result = personImportService.importPersonFile(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en la importación: " + e.getMessage());
        }
    }

    @PostMapping("/team-person")
    public ResponseEntity<?> importTeamPerson(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean skipHeader,
            @RequestParam Integer userId
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor seleccione un archivo válido.");
        }
        try {
            var result = teamPersonimportService.importCsv(file, userId, skipHeader);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en la importación: " + e.getMessage());
        }
    }

    @PostMapping("/results")
    public ResponseEntity<?> importResults(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean skipHeader,
            @RequestParam Integer userId
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor seleccione un archivo válido.");
        }
        try {
            var result = resultImportService.importCsv(file, userId, skipHeader);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en la importación: " + e.getMessage());
        }
    }

    @PostMapping("/tournament-registrations")
    public ResponseEntity<?> importTournamentRegistrations(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean skipHeader,
            @RequestParam Integer userId
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor seleccione un archivo válido.");
        }
        try {
            var result = tournamentRegistrationImportService.importCsv(file, userId, skipHeader);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en la importación: " + e.getMessage());
        }
    }
}