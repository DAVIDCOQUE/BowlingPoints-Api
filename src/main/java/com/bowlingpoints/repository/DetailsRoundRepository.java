package com.bowlingpoints.repository;

import com.bowlingpoints.entity.DetailsRound;
import com.bowlingpoints.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailsRoundRepository extends JpaRepository<DetailsRound,Integer> {

    List<DetailsRound> findByIdRonda(Integer rondaId);

}
