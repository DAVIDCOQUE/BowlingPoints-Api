package com.bowlingpoints.controller;


import com.bowlingpoints.dto.EventDetails;
import com.bowlingpoints.dto.EventsDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.TypeEventsDTO;
import com.bowlingpoints.entity.Evento;
import com.bowlingpoints.service.EventServices;
import com.bowlingpoints.service.TypeEventServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("events/v1")
public class EventsController {

    @Autowired
    TypeEventServices typeEventServices;

    @Autowired
    EventServices eventServices;

    @GetMapping(value="eventByType/{eventType}")
    public ResponseGenericDTO<List<EventsDTO>> getAllTypeEvents(@PathVariable String eventType) {

        List<EventsDTO> event = eventServices.getEventByType(eventType);

        return new ResponseGenericDTO<>(true,"Lista de tipos de eventos entregada con exito",event);
    }

    @GetMapping(value="allEvents")
    public ResponseGenericDTO<List<EventsDTO>> getAllEvents() {

        List<EventsDTO> eventServicesList = eventServices.getAllEvents();

        return new ResponseGenericDTO<>(true,"Lista de eventos entregada con exito",eventServicesList);
    }

    @GetMapping(value="getEvents")
    public ResponseGenericDTO<List<Evento>> getEvents() {

        List<Evento> eventServicesList = eventServices.eventList();

        return new ResponseGenericDTO<>(true,"Lista de eventos entregada con exito",eventServicesList);
    }

    @GetMapping(value="/findEventDetails/{id}")
    public ResponseGenericDTO<EventDetails> findEventDetails(@PathVariable int id){

        EventDetails eventDetails = new EventDetails();

        eventDetails = eventServices.getEventDetails(id);

        return new ResponseGenericDTO<>(true,"Consulta realizada con exito",eventDetails);

    }

    @GetMapping(value="/eventByStatus/{statusEvent}")
    public ResponseGenericDTO<List<EventsDTO>> findEventByStatus(@PathVariable String statusEvent){

        List<EventsDTO> event = eventServices.getEventByStatus(statusEvent);

        return new ResponseGenericDTO<>(true,"Consulta realizada con exito",event);

    }

    @GetMapping(value="/eventByStatus/{statusEvent}/{idTypeEvent}")
    public ResponseGenericDTO<List<EventDetails>> findEventByIdAndStatus(@PathVariable String statusEvent
            , @PathVariable String idTypeEvent){

        List<EventDetails> event = eventServices.getEventByStatusAndTypeEvent(statusEvent, idTypeEvent);

        return new ResponseGenericDTO<>(true,"Consulta realizada con exito",event);

    }

}
