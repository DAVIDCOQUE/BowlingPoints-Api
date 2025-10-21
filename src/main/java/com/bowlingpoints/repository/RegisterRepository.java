package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRepository extends JpaRepository<TournamentRegistration, Integer> {


}
