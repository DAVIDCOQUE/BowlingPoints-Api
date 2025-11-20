package com.bowlingpoints.controller;

import com.bowlingpoints.service.PersonImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    @Autowired
    private PersonImportService personImportService;

    @PostMapping("/persons")
    public ResponseEntity<?> importPersons(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor seleccione un archivo válido.");
        }

        try {
            String result = personImportService.importPersonFile(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en la importación: " + e.getMessage());
        }
    }
}