package com.bowlingpoints.service;


import com.bowlingpoints.dto.EventsDTO;
import com.bowlingpoints.entity.Event;
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

        List<Event> eventEntityList =  eventRepository.findAll();

        List<EventsDTO> eventsDTOList = new ArrayList<>();

        eventEntityList.forEach(
                event->{
                    eventsDTOList.add(EventsDTO.builder().
                            nameEvent(event.getNombreEvento())
                            .eventOrganizer(null)
                            .descriptionEvent(null).build());
                }
        );
        return eventsDTOList;
    }

    public List<Event> eventList(){

        return eventRepository.findAll();
    }

}
