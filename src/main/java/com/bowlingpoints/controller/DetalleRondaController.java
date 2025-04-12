package com.bowlingpoints.controller;


import com.bowlingpoints.dto.EventoRamasDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.service.BranchEventServices;
import com.bowlingpoints.service.DetailsRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("detalleRonda/v1")
public class DetalleRondaController {

    @Autowired
    DetailsRoundService detailsRoundService;

    @GetMapping(value = "/all")
    public ResponseGenericDTO<List<DetailsRound>> getAllDetailsRound() {

        List<DetailsRound> listDetailsRound = detailsRoundService.getDetailsRound();

        return new ResponseGenericDTO<>(true,"Lista de detalles de rondas entregada con exito",listDetailsRound);
    }
}
