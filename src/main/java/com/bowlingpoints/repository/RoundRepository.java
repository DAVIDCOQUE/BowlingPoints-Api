package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Ronda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoundRepository extends JpaRepository<Ronda,Integer> {

    List<Ronda> findByIdEvento(Integer idEvento);


}
