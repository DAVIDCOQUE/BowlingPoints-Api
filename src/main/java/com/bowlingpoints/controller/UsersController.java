package com.bowlingpoints.controller;


import com.bowlingpoints.dto.PersonaDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.Persona;
import com.bowlingpoints.entity.Ronda;
import com.bowlingpoints.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("users/v1")
public class UsersController {

    @Autowired
    PersonaService personaService;

    @GetMapping(value = "/all")
    public ResponseGenericDTO<List<PersonaDTO>> getAllPersona() {

        List<PersonaDTO> personaList = personaService.getAllPersona();

        return new ResponseGenericDTO<>(true,"Lista de personas entregada con exito",personaList);
    }

    @PutMapping("/persona-update/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> updatePersona(@PathVariable Integer id, @RequestBody PersonaDTO input) {
        try {
            boolean updated = personaService.updatePersona(id, input);

            if (!updated) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ResponseGenericDTO<>(false, "Persona no encontrada", null));
            }

            return ResponseEntity
                    .ok(new ResponseGenericDTO<>(true, "Persona actualizada correctamente", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseGenericDTO<>(false, e.getMessage(), null));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseGenericDTO<>(false, "Error interno al actualizar la persona", null));
        }
    }

    @DeleteMapping("/persona-delete/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> deletePersona(@PathVariable Integer id) {
        boolean deleted = personaService.deletePersona(id);

        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseGenericDTO<>(false, "Persona no encontrada", null));
        }

        return ResponseEntity
                .ok(new ResponseGenericDTO<>(true, "Persona eliminada correctamente", null));
    }

}
