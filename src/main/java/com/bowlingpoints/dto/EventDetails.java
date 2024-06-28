package com.bowlingpoints.dto;


import com.bowlingpoints.entity.Rama;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetails {

    private DetalleEvento detalleEvento;
    private List<EventoRamasDTO> eventoRamasDTO;

}
