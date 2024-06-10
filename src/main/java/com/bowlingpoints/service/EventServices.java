package com.bowlingpoints.service;


import com.bowlingpoints.dto.EventsDTO;
import com.bowlingpoints.entity.Evento;
import com.bowlingpoints.enums.EnumsTypeEvents;
import com.bowlingpoints.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventServices {

    @Autowired
    EventRepository eventRepository;

    public List<EventsDTO> getAllEvents(){

        List<Evento> eventEntityList =  eventRepository.findAll();

        List<EventsDTO> eventsDTOList = new ArrayList<>();

        eventEntityList.forEach(
                event->{

                    String nameConcat = event.getUsuario().getPersona().getPrimerNombre();

                    eventsDTOList.add(EventsDTO.builder().
                            nameEvent(event.getNombreEvento())
                            .eventOrganizer(event.getUsuario().getPersona().getPrimerNombre()+" "+
                                    event.getUsuario().getPersona().getPrimerApellido())
                            .descriptionEvent(null).build());
                }
        );
        return eventsDTOList;
    }

    public List<EventsDTO> getEventByType(String tipoEvento){

        Integer tipoEventoId = EnumsTypeEvents.valueOf(tipoEvento).getIdentificador();

        List<Evento> eventoList = eventRepository.findByIdTipoEvento(tipoEventoId);

        List<EventsDTO> eventsDTOList = new ArrayList<>();

        eventoList.forEach(
                evento->{
                    String nameConcat = evento.getUsuario().getPersona().getPrimerNombre();
                    eventsDTOList.add(EventsDTO.builder().nameEvent(evento.getNombreEvento())
                            .eventOrganizer(nameConcat)
                            .descriptionEvent(evento.getDescripcion()).build());
                }
        );

        return eventsDTOList;
    }

    public List<Evento> eventList(){

        return eventRepository.findAll();
    }

    public List<EventsDTO> getEventByStatus(){




        return null;
    }

}
