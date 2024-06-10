package com.bowlingpoints.controller;


import com.bowlingpoints.dto.TypeEventsDTO;
import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.service.DetailsRoundService;
import com.bowlingpoints.service.TypeEventServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("typeEvento/v1")
public class TypeEventController {

    @Autowired
    private final TypeEventServices typeEventServices;

    @GetMapping(value="tipoEvento")
    public TypeEventsDTO getAll() {

        return typeEventServices.getTypeEvents();
    }
}
