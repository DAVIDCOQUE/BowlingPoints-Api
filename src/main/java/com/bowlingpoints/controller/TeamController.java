package com.bowlingpoints.controller;

import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TeamDTO;
import com.bowlingpoints.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<List<TeamDTO>>> getAll() {
        List<TeamDTO> list = teamService.getAll();
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Equipos cargados correctamente", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<TeamDTO>> getById(@PathVariable Integer id) {
        TeamDTO dto = teamService.getById(id);
        return dto != null
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Equipo encontrado", dto))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ResponseGenericDTO<TeamDTO>> create(@RequestBody TeamDTO dto) {
        TeamDTO created = teamService.create(dto);
        return ResponseEntity.ok(new ResponseGenericDTO<>(true, "Equipo creado correctamente", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody TeamDTO dto) {
        return teamService.update(id, dto)
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Equipo actualizado", null))
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        return teamService.delete(id)
                ? ResponseEntity.ok(new ResponseGenericDTO<>(true, "Equipo eliminado", null))
                : ResponseEntity.notFound().build();
    }
}
