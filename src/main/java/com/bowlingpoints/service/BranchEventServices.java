package com.bowlingpoints.service;


import com.bowlingpoints.dto.EventoRamasDTO;
import com.bowlingpoints.entity.Evento;
import com.bowlingpoints.entity.EventoRama;
import com.bowlingpoints.repository.BranchEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BranchEventServices {

    @Autowired
    BranchEventRepository branchEventRepository;


    public List<EventoRamasDTO> findByIdEvento(int idEvento){

        List<EventoRama> eventoRamaList= branchEventRepository.findByIdEvento(idEvento);

        List<EventoRamasDTO> ramasDTOList = new ArrayList<>();

        eventoRamaList.forEach(
                rama -> {
                    ramasDTOList.add(EventoRamasDTO.builder()
                                    .nombreRama(rama.getBranch().getDescription())
                                    .cantidadJugadores(1)
                            .build());
                }
        );
        return ramasDTOList;
    }
}
