package com.bowlingpoints.service;


import com.bowlingpoints.dto.TypeEventsDTO;
import com.bowlingpoints.entity.TypeEvent;
import com.bowlingpoints.repository.TypeEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TypeEventServices {

    @Autowired
    TypeEventRepository typeEventRepository;

    //Return all of types events available 15-05-2024 JSoto
    public TypeEventsDTO getTypeEvents(){
        TypeEventsDTO typeEventsDTO = new TypeEventsDTO();
        List<String> listOfStrings = new ArrayList<>();
        List<TypeEvent> typeEventList = typeEventRepository.findAll();

        typeEventList.forEach(typeEvent -> {
            listOfStrings.add(typeEvent.getDescripcion());
                }
        );

        typeEventsDTO.setListTypeEvents(listOfStrings);

        return typeEventsDTO;
    }

}
