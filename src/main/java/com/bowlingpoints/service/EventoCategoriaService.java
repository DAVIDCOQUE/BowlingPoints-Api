package com.bowlingpoints.service;


import com.bowlingpoints.dto.EventoCategorias;
import com.bowlingpoints.entity.EventoCategoria;
import com.bowlingpoints.repository.EventoCategoriaRepository;
import jdk.jfr.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventoCategoriaService {

    @Autowired
    EventoCategoriaRepository eventoCategoriaRepository;


    public List<EventoCategorias> getCategoriaByIdEvento(int idEvento){


        List<EventoCategoria> eventoCategoriaList = eventoCategoriaRepository.findByIdEvento(idEvento);

        List<EventoCategorias> eventoCategorias = new ArrayList<>();

        eventoCategoriaList.forEach(
                eventoCategoria -> {
                    eventoCategorias.add(EventoCategorias.builder()
                                    .nombreCategoria(eventoCategoria.getCategoria().getNombreCategoria())
                                    .numeroJugadores(1)
                            .build());
                }
        );
        return eventoCategorias;
    }
}
