package com.bowlingpoints.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DetalleEvento {

    public String organizador;
    public String nombreEvento;
    public Date fechaInicio;
    public Date fechaFin;
}
