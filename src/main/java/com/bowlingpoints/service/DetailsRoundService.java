package com.bowlingpoints.service;


import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.repository.DetailsRoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Service
public class DetailsRoundService {

    @Autowired
    DetailsRoundRepository detailsRoundRepository;

    public List<DetailsRound> getDetailsRound(){
        return detailsRoundRepository.findAll();
    }

    public List<DetailsRound> getDetailsRoundByRound(Integer rondaId){
        return detailsRoundRepository.findByIdRonda(rondaId);
    }
}
