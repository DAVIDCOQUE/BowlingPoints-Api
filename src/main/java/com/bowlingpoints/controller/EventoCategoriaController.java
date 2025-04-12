package com.bowlingpoints.controller;


import com.bowlingpoints.dto.EventoCategorias;
import com.bowlingpoints.dto.EventoRamasDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.EventoCategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("eventoCategoria/v1")
public class EventoCategoriaController {


    @Autowired
    EventoCategoriaService eventoCategoriaService;

    @GetMapping(value="getEventoCategoria/{idEvento}")
    public ResponseGenericDTO<List<EventoCategorias>> getEventoCategoria(@PathVariable int idEvento) {

        List<EventoCategorias> eventoCategoriasList = eventoCategoriaService.getCategoriaByIdEvento(idEvento);

        return new ResponseGenericDTO<>(true,"Lista de ramas entregada con exito",eventoCategoriasList);
    }
}
