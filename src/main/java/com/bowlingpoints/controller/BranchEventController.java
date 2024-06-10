package com.bowlingpoints.controller;


import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.EventoRama;
import com.bowlingpoints.service.BranchEventServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("branchEvent/v1")
public class BranchEventController {


    @Autowired
    BranchEventServices branchEventServices;


    @GetMapping(value="branchEvent")
    public ResponseGenericDTO<EventoRama> getAllTypeEvents() {

        EventoRama typeEventsDTO = branchEventServices.findById();

        return new ResponseGenericDTO<>(true,"Lista de tipos de eventos entregada con exito",typeEventsDTO);
    }


}
