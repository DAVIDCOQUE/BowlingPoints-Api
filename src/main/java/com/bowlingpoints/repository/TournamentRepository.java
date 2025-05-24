package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament,Integer> {


}
