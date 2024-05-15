package com.bowlingpoints.controller;

import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.service.DetailsRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("points/v1")
public class PointsController {

    @Autowired
    private final DetailsRoundService detailsRoundService;

    @GetMapping(value="points")
    public List<DetailsRound> getAll() {

        return detailsRoundService.getDetailsRound();
    }
}
