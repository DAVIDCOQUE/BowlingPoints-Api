package com.bowlingpoints.service;


import com.bowlingpoints.dto.RondaDTO;
import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.entity.Ronda;
import com.bowlingpoints.repository.DetailsRoundRepository;
import com.bowlingpoints.repository.RoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RondaService {

    @Autowired
    RoundRepository roundRepository;

    @Autowired
    DetailsRoundService detailsRoundService;

    public List<Ronda> getRondaAll(){
        return roundRepository.findAll();
    }

    public List<RondaDTO> getPointsByEvent(Integer idEvento){

        List<Ronda> listRonda = roundRepository.findByIdEvento(idEvento);

        List<RondaDTO> rondaDTOList = new ArrayList<>();

        for (Ronda ronda : listRonda) {
            List<DetailsRound> detailsRoundList = detailsRoundService.getDetailsRoundByRound(ronda.getIdRonda());
            List<Integer>  integerList =  new ArrayList<Integer>();
            int sumTotal = 0;
            for (DetailsRound detailsRound : detailsRoundList) {
                integerList.add(detailsRound.getPuntacion());
                sumTotal = sumTotal + detailsRound.getPuntacion();
            }
            RondaDTO rondaDto = RondaDTO.builder()
                    .total(sumTotal)
                    .juegos(integerList)
                    .nombres(ronda.getPersona().getFirstName() + " " +ronda.getPersona().getSecondName())
                    .apellidos(ronda.getPersona().getLastname() + " "+ronda.getPersona().getSecondLastname())
                    .promedio((double) sumTotal /integerList.size())
                    .build();
            rondaDTOList.add(rondaDto);
        }
        return this.sortHighGame(rondaDTOList);
    }

    public List<RondaDTO> sortHighGame(List<RondaDTO> rondaDTOList){
        Collections.sort((rondaDTOList));
        for (int i = 0; i < rondaDTOList.size(); i++) {
            rondaDTOList.get(i).setPuesto(i+1);
        }
        return rondaDTOList;
    }
}
