package com.bowlingpoints.controller;


import com.bowlingpoints.dto.EventsDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TypeEventsDTO;
import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.service.EventServices;
import com.bowlingpoints.service.TypeEventServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("events/v1")
public class EventsController {

    @Autowired
    TypeEventServices typeEventServices;

    @Autowired
    EventServices eventServices;

    @GetMapping(value="typeEvents")
    public ResponseGenericDTO<TypeEventsDTO> getAllTypeEvents() {

        TypeEventsDTO typeEventsDTO = typeEventServices.getTypeEvents();

        return new ResponseGenericDTO<>(true,"Lista de tipos de eventos entregada con exito",typeEventsDTO);
    }

    @GetMapping(value="allEvents")
    public ResponseGenericDTO<List<EventsDTO>> getAllEvents() {

        List<EventsDTO> eventServicesList = eventServices.getAllEvents();

        return new ResponseGenericDTO<>(true,"Lista de eventos entregada con exito",eventServicesList);
    }
}
