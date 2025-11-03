package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    // Method to find a team by its name
    Optional<Team> findByNameTeam(String nameTeam);
}
