package com.bowlingpoints.controller;


import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.RondaDTO;
import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.entity.Ronda;
import com.bowlingpoints.service.DetailsRoundService;
import com.bowlingpoints.service.RondaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("ronda/v1")
public class RondaController {

    @Autowired
    RondaService rondaService;

    @GetMapping(value = "/all")
    public ResponseGenericDTO<List<Ronda>> getAllDetailsRound() {

        List<Ronda> listRonda = rondaService.getRondaAll();

        return new ResponseGenericDTO<>(true,"Lista de detalles de rondas entregada con exito",listRonda);
    }

    @GetMapping(value = "/{idEvent}")
    public ResponseGenericDTO<List<RondaDTO>> getPuntuacionesByEvent(@PathVariable int idEvent) {

        List<RondaDTO> listRonda = rondaService.getPointsByEvent(idEvent);

        return new ResponseGenericDTO<>(true,"Lista de detalles de rondas entregada con exito",listRonda);
    }
}
