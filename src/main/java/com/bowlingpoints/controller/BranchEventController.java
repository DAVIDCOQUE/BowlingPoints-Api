package com.bowlingpoints.controller;


import com.bowlingpoints.dto.EventoRamasDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.EventoRama;
import com.bowlingpoints.service.BranchEventServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("branchEvent/v1")
public class BranchEventController {


    @Autowired
    BranchEventServices branchEventServices;


    @GetMapping(value="branchEvent/{idEvento}")
    public ResponseGenericDTO<List<EventoRamasDTO>> getAllTypeEvents(@PathVariable int idEvento) {

        List<EventoRamasDTO> typeEventsDTO = branchEventServices.findByIdEvento(idEvento);

        return new ResponseGenericDTO<>(true,"Lista de ramas entregada con exito",typeEventsDTO);
    }


}
