package com.bowlingpoints.controller;

import com.bowlingpoints.dto.AmbitDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.AmbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ambits")
@RequiredArgsConstructor
public class AmbitController {

    private final AmbitService ambitService;

    @GetMapping("/all-ambit")
    public ResponseEntity<ResponseGenericDTO<List<AmbitDTO>>> getAll() {
        return ResponseEntity.ok(ambitService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<AmbitDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ambitService.getById(id));
    }

    @PostMapping("/save-ambit")
    public ResponseEntity<ResponseGenericDTO<AmbitDTO>> create(@RequestBody AmbitDTO dto) {
        return ResponseEntity.ok(ambitService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> update(@PathVariable Integer id, @RequestBody AmbitDTO dto) {
        return ResponseEntity.ok(ambitService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseGenericDTO<Void>> delete(@PathVariable Integer id) {
        return ResponseEntity.ok(ambitService.delete(id));
    }
}
