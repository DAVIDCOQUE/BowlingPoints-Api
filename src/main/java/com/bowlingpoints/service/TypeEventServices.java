package com.bowlingpoints.service;


import com.bowlingpoints.dto.TypeEventsDTO;
import com.bowlingpoints.entity.TipoEvento;
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
        List<TipoEvento> typeEventList = typeEventRepository.findAll();

        typeEventList.forEach(event -> {
            listOfStrings.add(event.getDescripcion());
                }
        );

        typeEventsDTO.setListTypeEvents(listOfStrings);

        return typeEventsDTO;
    }

}
