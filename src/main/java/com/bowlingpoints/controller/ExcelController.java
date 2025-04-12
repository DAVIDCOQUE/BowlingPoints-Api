package com.bowlingpoints.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bowlingpoints.service.ExcelService;
import com.bowlingpoints.util.Jugador;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/jugadores")
public class ExcelController {

    @Autowired
    private ExcelService jugadorService;

    @PostMapping("/upload")
    public List<Jugador> obtenerJugadores(@RequestParam("file") MultipartFile file) {
        return jugadorService.obtenerJugadoresDesdeArchivo(file);
    }
}
